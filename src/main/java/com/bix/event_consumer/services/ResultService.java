package com.bix.event_consumer.services;


import com.bix.event_consumer.enums.BetStatus;
import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.evaluator.MarketEvaluator;
import com.bix.event_consumer.events.BetStatusUpdate;
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
    private final TransactionService transactionService;
    private final Map<String, MarketEvaluator> marketEvaluator;

    @Transactional
    public void processBetResults(String eventId){
        log.info("Processing results for event {}",eventId);

        // 01. Get event's final score
        Score score = this.scoreRepository.findScoreByEventId(eventId);
        if(score == null){
            log.warn("No final score for event {}",eventId);
            return;
        }

        // 02. Get event's pending slips
        List<Slip> pendingSlips = betSlipRepository.findEventsPendingSlips(eventId);

        // 03. Nobody placed a bet on the event ? exit gracefully
        if(pendingSlips.isEmpty()){
            log.info("No pending slips for event {} ", eventId);
            return;
        }

        // 04. Evaluate each slip
        pendingSlips.forEach(slip -> {
            int newStatus = this.evaluateSlip(slip, score);
            betSlipRepository.updateSlipStatus(slip.getBetSlipId(),newStatus);
            log.info("Slip Id {} updated to status {}",
                    slip.getBetSlipId(),
                    newStatus);
        });

        // 05. Update parent bet status
        this.updateBetStatus(pendingSlips);
        log.info("Completed result processing for event {}", eventId);

    }


    private int checkBetStatus(List<Slip> slips){
        // 1. RULE 01: If any slip is LOST, the whole bet is LOST immediately
        if(slips.stream().anyMatch(slip -> slip.getStatus()
                .equals(BetStatus.LOST.getStatus()))){
            return BetStatus.LOST.getStatus();
        }

        // 2. RULE 02: If there is still a PENDING slip, the whole bet is PENDING
        if(slips.stream().anyMatch(slip -> slip.getStatus()
                .equals(BetStatus.PENDING.getStatus()))){
            return BetStatus.PENDING.getStatus();
        }

        // 3. RULE 03: If ALL slips are VOID the bet is VOID
        if(slips.stream().allMatch(slip -> slip.getStatus()
                .equals(BetStatus.VOID.getStatus()))){
            return BetStatus.VOID.getStatus();
        }

        // 4. RULE 04: If here, it means there are NO losses and NO pendings
        // The slip is mix of WON and VOID
        // Mix of WON + VOID = WON
        return BetStatus.WON.getStatus();
    }

    private void updateBetStatus(List<Slip> slips){
        // 01. Group slips by bet_id
        Map<Long, List<Slip>> slipsByBet = slips.stream()
                .collect(Collectors.groupingBy(Slip::getBetId));

        // 02. Get all the slips for this bet
        slipsByBet.forEach((betId,betSlips)->{
            // a. look for slips with the betId
            List<Slip> allSlips = this.betSlipRepository.findBetsSlip(betId);

            // b. return the bet status
            int betStatus = this.checkBetStatus(allSlips);

            // c. update the bet status
            BetStatusUpdate updates = this.betRepository.updateBetStatus(betId,betStatus);

            // d. publish the update to the transaction queue
            this.transactionService.publishBetStatus(updates);

            log.info("Bet {} with status {}  updated to {} ", betId, betStatus, updates);
        });
    }


    private int evaluateSlip(Slip slip, Score score){
        //Check the void status of a game.
        // Void if game was canceled, postponed, suspended or forfeited
      if(this.isVoidStatus(score.getEventStatus().getCode())){
          log.info(
                  "Slip {} voided because of event {} status.",
                  slip.getBetSlipId(),score.getEventStatus().getCode());
          return SlipStatus.VOID.getStatus();
      }

      // Get the strategy to evaluate the markets
        String marketName = slip.getMarketName().toLowerCase().trim();
        String strategyKey = marketName + "Evaluator";
        MarketEvaluator strategy = marketEvaluator.get(strategyKey);

        if(strategy == null){
            log.warn("Missing strategy for market {}", marketName);
            return SlipStatus.PENDING.getStatus();
        }

        return strategy.evaluate(slip,score);
    }


    private boolean isVoidStatus(int eventStatus){
        // Checks whether a bet has void status.
        // If void returns true else returns false
        return eventStatus == EventStatus.STATUS_CANCELED.getCode()
                || eventStatus == EventStatus.STATUS_POSTPONED.getCode()
                || eventStatus == EventStatus.STATUS_SUSPENDED.getCode()
                || eventStatus == EventStatus.STATUS_FORFEIT.getCode();
    }
}
