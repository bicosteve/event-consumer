package com.bix.event_consumer.services;


import com.bix.event_consumer.enums.BetStatus;
import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import com.bix.event_consumer.repositories.BetRepository;
import com.bix.event_consumer.repositories.BetSlipRepository;
import com.bix.event_consumer.repositories.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResultService{
    private final ScoreRepository scoreRepository;
    private final BetSlipRepository betSlipRepository;
    private final BetRepository betRepository;


    @Transactional
    public void processBetResults(String eventId){
        log.info("ResultService::processing results for event {} ",eventId);

        // 01. Get event's final score
        Score score = this.scoreRepository.findScoreByEventId(eventId);
        if(score == null){
            log.warn("ResultService::no final score for event {} ",eventId);
            return;
        }

        // 02. Get event's pending slips
        List<Slip> pendingSlips = betSlipRepository.findEventsPendingSlips(eventId);

        // 03. Nobody placed a bet on the event ?
        // Exit gracefully
        if(pendingSlips.isEmpty()){
            log.info("ResultService::no pending slips for event {} ",eventId);
            return;
        }


        // 04. Evaluate each slip
        pendingSlips.forEach(slip -> {
            int slipStatus = this.evaluateSlip(slip,score);
            betSlipRepository.updateSlipStatus(slip.getBetSlipId(),slipStatus);
            log.info("ResultService::slip {} updated to status {}",slip.getBetSlipId(),slipStatus);
        });

        // 05. Update parent bet status
        this.updateBetStatus(pendingSlips);

        log.info("ResultService::results processed for event {} ", eventId);

    }



    private int checkBetStatus(List<Slip> slips){
        // Checks the status of the slips associated with bet and returns
        // an Integer. This integer will be used to set the overall bet status
        boolean hasLost = slips.stream()
                .anyMatch(slip -> slip.getStatus() == SlipStatus.LOST.getStatus());

        boolean allWon  = slips.stream()
                .allMatch(slip -> slip.getStatus() == SlipStatus.WON.getStatus());

        boolean allVoid = slips.stream()
                .allMatch(slip -> slip.getStatus() == SlipStatus.VOID.getStatus());

        boolean hasPending = slips.stream()
                .anyMatch(slip -> slip.getStatus() == SlipStatus.PENDING.getStatus());

        if(hasLost) return BetStatus.LOST.getStatus();
        if(hasPending) return BetStatus.PENDING.getStatus();
        if(allWon) return BetStatus.WON.getStatus();
        if(allVoid) return BetStatus.VOID.getStatus();

        return BetStatus.WON.getStatus();
    }

    private void updateBetStatus(List<Slip> slips){
        // 01. Group slips by bet_id
        Map<Long, List<Slip>> slipsByBet = slips.stream()
                .collect(Collectors.groupingBy(Slip::getBetId));

        // 02. Get all the slips for this bet
        slipsByBet.forEach((betId,betSlips)->{
            List<Slip> allSlips = this.betSlipRepository.findBetsSlip(betId);
            int betStatus = this.checkBetStatus(allSlips);
            this.betRepository.updateBetStatus(betId,betStatus);
            log.info("ResultService:: bet {} updated to status {} ",betId,betStatus);
        });
    }


    private int evaluateSlip(Slip slip, Score score){
        // Void if game was canceled, postponed, suspended or forfeited
      if(isVoidStatus(score.getEventStatus().getCode())){
          log.info(
                  "ResultService::slip {} voided due to event status {}",
                  slip.getBetSlipId(),score.getEventStatus().getCode()
          );

          return SlipStatus.VOID.getStatus();
      }

      // market_names moneyline, handicap, totals
        return switch(slip.getMarketName()){
          case "moneyline" -> this.evaluateMoneylineMarket(slip,score);
         // case "handicap" -> this.evaluateHandicap(slip,score);
         // case "totals" -> this.evaluateTotalsMarket(slip,score);
          default -> {
              log.warn("ResultService::Unknown market type {} ", slip.getMarketName());
              yield SlipStatus.VOID.getStatus();
          }
        };
    }

    // Evaluate Markets

    // 01. Moneyline
    private int evaluateMoneylineMarket(Slip slip, Score score){
        int marketIdPick = slip.getMarketId();

        // 01. Check if the pick was draw (3)
        if(marketIdPick == 3){
            boolean isDraw = score.getWinnerAway() == 0 && score.getWinnerHome() == 0;
            return isDraw ? SlipStatus.WON.getStatus() : SlipStatus.LOST.getStatus();
        }

        // 02. Check home team was selected to win
        if(marketIdPick == score.getTeamIdHome()){
            return score.getWinnerHome() == 1 ? SlipStatus.WON.getStatus() : SlipStatus.LOST.getStatus();
        }

        // 03. Check away team was selected to win
        if(marketIdPick == score.getTeamIdAway()){
            return score.getWinnerAway() == 1 ? SlipStatus.WON.getStatus() : SlipStatus.LOST.getStatus();
        }

        // Log a warning when pick is not matching moneyline picks
        log.warn(
                "ResultService::Moneyline slip {} participant {} not matched",
                slip.getMarketId(),marketIdPick
        );

        return SlipStatus.VOID.getStatus();
    }

    // 02. Evaluate handicap
//    private int evaluateHandicap(Slip slip, Score score){
//
//        return 0;
//    }

    // 03. Evaluate totals market
//    private int evaluateTotalsMarket(Slip slip, Score score){
//
//        return 0;
//    }


    private boolean isVoidStatus(int eventStatus){
        // Checks whether a bet has void status.
        // If void returns true else returns false
        return eventStatus == EventStatus.STATUS_CANCELED.getCode()
                || eventStatus == EventStatus.STATUS_POSTPONED.getCode()
                || eventStatus == EventStatus.STATUS_SUSPENDED.getCode()
                || eventStatus == EventStatus.STATUS_FORFEIT.getCode();
    }





}
