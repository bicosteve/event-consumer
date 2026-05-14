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

        // Totals market has teams id of 9
        int teamIdPick = slip.getTeamId();
        if(teamIdPick != 9){
            return SlipStatus.PENDING.getStatus();
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

        // Get all the goals score in the event.
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
                "Totals slip {} win condition for team {} not matched home = {} away = {}",
                slip.getBetSlipId(),
                teamIdPick,
                score.getScoreHome(),
                score.getScoreAway()
        );

        return  SlipStatus.LOST.getStatus();
    }
}
