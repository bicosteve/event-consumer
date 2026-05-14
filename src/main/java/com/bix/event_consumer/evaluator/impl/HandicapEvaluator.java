package com.bix.event_consumer.evaluator.impl;

import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.evaluator.MarketEvaluator;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component("handicapEvaluator")
public class HandicapEvaluator implements MarketEvaluator {
    // These are high scoring sportsId which will affect the handicap value
    private static final List<Integer> sportIds = new ArrayList<>(Arrays.asList(1,2,4,5,8,9,20,21));


    @Override
    public int evaluate(Slip slip, Score score){
        // Reject the bet if special bet value is empty string or null
        if(slip.getSpecialBetValue() == null
                || slip.getSpecialBetValue().isBlank()){
            return SlipStatus.PENDING.getStatus();
        }

        //
        int teamIdPick = slip.getTeamId();
        double handicap = 0;
        double adjustedScore;

        // Isolate the value by removing splitting the string and taking the second portion
        // comes in this format hcp=2.5
        String rawValue = slip.getSpecialBetValue();
        if(rawValue.contains("=")){
            try{
                String numberValue = slip.getSpecialBetValue().split("=")[1];
                handicap = Double.parseDouble(numberValue);
            }catch(NumberFormatException e){
                log.warn("Invalid special bet value {} ", slip.getSpecialBetValue());
                return SlipStatus.PENDING.getStatus();
            }

        }

        handicap = calculateHandicap(slip.getSportId(),score,handicap);


        // If Home team was selected.
        if(teamIdPick == score.getTeamIdHome()){
            adjustedScore = score.getScoreHome();
            log.info("Handicap home team {} adjusted score {} ", teamIdPick, adjustedScore);
            return this.determineStatus(adjustedScore,handicap);
        }

        // If Away team was selected
        if(teamIdPick == score.getTeamIdAway()){
            adjustedScore = score.getScoreAway();
            log.info("Handicap away team {} adjusted score {} ", teamIdPick, adjustedScore);
           return this.determineStatus(adjustedScore,handicap);
        }

        // Log handicap pick was not matched
        log.warn(
                "Handicap slip {} team {} not matched home = {} away = {}",
                slip.getBetSlipId(),
                teamIdPick,
                score.getTeamIdHome(),
                score.getScoreAway()
                );

        return  SlipStatus.VOID.getStatus();
    }

    private double calculateHandicap(Integer sportId, Score score, double originalHandicap){
        if(sportIds.contains(sportId)){
            return (double) score.getScoreHome() - score.getScoreAway();
        }

        return originalHandicap;
    }

    private int determineStatus(double adjustedScore, double handicapValue){
        if(adjustedScore > handicapValue){
            return SlipStatus.WON.getStatus();
        } else if(adjustedScore <= handicapValue){
            return SlipStatus.LOST.getStatus();
        }

        return SlipStatus.PENDING.getStatus();

    }
}
