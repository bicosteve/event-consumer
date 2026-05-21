package com.bix.event_consumer.evaluator.impl;

import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.evaluator.MarketEvaluator;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("totalsEvaluator")
public class TotalsEvaluator implements MarketEvaluator {
    @Override
    public int evaluate(Slip slip, Score score){
        // Reject the bet if special bet value is empty string or null
        if(slip.getSpecialBetValue() == null
                || slip.getSpecialBetValue().isBlank()){
            return SlipStatus.PENDING.getStatus();
        }

        // Totals market has teams id of 9 & 10
        int teamIdPick = slip.getTeamId();
        if(teamIdPick != 9 && teamIdPick != 10){
            // teamId 9 is over and teamId 10 is under
            log.warn(
                    "Voiding slip due to invalid totals market pick {}",
                    teamIdPick
            );
            return SlipStatus.VOID.getStatus();
        }

        // Totals participant names can be over or under
        if(!slip.getParticipantName().equalsIgnoreCase("under")
                && !slip.getParticipantName().equalsIgnoreCase("over")){
            // participantName can only be either over or under
            // void the slip
            log.warn(
                    "Voiding slip due to invalid participant name {}",
                    slip.getParticipantName()
            );
            return SlipStatus.VOID.getStatus();
        }

        double handicapValue;

        try{
            // Isolate the handicap value by splitting the string and taking the second portion
            // The string comes in this format hcp=2.5
            String slipSpecialBetValue = slip.getSpecialBetValue().split("=")[1];
            handicapValue = Double.parseDouble(slipSpecialBetValue);
        }catch(NumberFormatException e){
            log.warn("Invalid special bet value {} ", slip.getSpecialBetValue());
            return SlipStatus.PENDING.getStatus();
        }

        // Calculate the total event's score
        double eventsTotalScore = score.getScoreHome() + score.getScoreAway();
        log.info(
                "Parsed handicap value {}, total event score {}",
                handicapValue,
                eventsTotalScore
        );


        if((slip.getParticipantName().equalsIgnoreCase("over")) && (eventsTotalScore > handicapValue)){
            return SlipStatus.WON.getStatus();
        }

        if((slip.getParticipantName().equalsIgnoreCase("under")) && (eventsTotalScore < handicapValue)){
            return SlipStatus.WON.getStatus();
        }


        // Log win condition for totals pick not matched
        log.warn(
                "Totals market slip {} win condition for team={} not matched home={} away={}",
                slip.getBetSlipId(),
                teamIdPick,
                score.getScoreHome(),
                score.getScoreAway()
        );

        return  SlipStatus.LOST.getStatus();
    }
}
