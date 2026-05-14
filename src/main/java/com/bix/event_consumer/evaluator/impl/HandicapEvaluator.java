package com.bix.event_consumer.evaluator.impl;

import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.evaluator.MarketEvaluator;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("handicapEvaluator")
public class HandicapEvaluator implements MarketEvaluator {


    @Override
    public int evaluate(Slip slip, Score score){
        if(slip.getSpecialBetValue() == null
                || slip.getSpecialBetValue().isBlank()){
            return SlipStatus.PENDING.getStatus();
        }


        double handicap = 0;
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


        int teamIdPick = slip.getTeamId();

        if(teamIdPick == score.getTeamIdHome()){
            double adjustedScoreAway = score.getScoreHome() + handicap;
            log.info(
                    "Handicap evaluation home team={} scoreHome={} handicap={} adjustedScore={} scoreAway={}",
                    teamIdPick,
                    score.getScoreHome(),
                    handicap,
                    adjustedScoreAway,
                    score.getScoreAway()
            );
            return this.determineStatus(adjustedScoreAway,score.getScoreHome());
        }

        // If Away team was selected
        if(teamIdPick == score.getTeamIdAway()){
            double adjustedScoreAway = score.getScoreAway() + handicap;
            log.info(
                    "Handicap evaluation away team={} scoreAway={} handicap={} adjustedScore={} scoreHome={}",
                    teamIdPick,
                    score.getScoreAway(),
                    handicap,
                    adjustedScoreAway,
                    score.getScoreHome()
            );
            return this.determineStatus(adjustedScoreAway,score.getScoreHome());
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


    private int determineStatus(double adjustedScore, double opponentScore){
        if(adjustedScore > opponentScore) return SlipStatus.WON.getStatus();
        return SlipStatus.LOST.getStatus(); // any other condition is LOST!
    }
}
