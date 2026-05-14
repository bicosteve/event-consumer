package com.bix.event_consumer.evaluator.impl;

import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.evaluator.MarketEvaluator;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("moneylineEvaluator")
public class MoneylineEvaluator implements MarketEvaluator {
    @Override
    public int evaluate(Slip slip, Score score){
        int teamIdPick = slip.getTeamId();

        // If draw, winner away and home will be 0 on scores table
        if(teamIdPick == 3){
            boolean isDraw = score.getWinnerAway() == 0 && score.getWinnerHome() == 0;
            return isDraw ? SlipStatus.WON.getStatus() : SlipStatus.LOST.getStatus();
        }

        // Check for home team selection
        if(teamIdPick == score.getTeamIdHome()){
            return score.getWinnerHome() == 1
                    ? SlipStatus.WON.getStatus()
                    : SlipStatus.LOST.getStatus();
        }

        // Check for away team selection
        if(teamIdPick == score.getTeamIdAway()){
            return score.getWinnerAway() == 1
                    ? SlipStatus.WON.getStatus()
                    : SlipStatus.LOST.getStatus();
        }

        log.warn("Moneyline slip {} team {} not matched home = {} or away = {}",
                slip.getBetSlipId(),
                teamIdPick,
                score.getTeamIdHome(),
                score.getTeamIdAway()
                );

        return SlipStatus.VOID.getStatus();
    }
}
