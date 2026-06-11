package com.bix.event_consumer.evaluator.impl;

import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneylineEvaluatorTest {

    private MoneylineEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new MoneylineEvaluator();
    }

    private Score buildScore(int home, int away, int winnerHome, int winnerAway) {
        return Score.builder()
                .teamIdHome(200)
                .teamIdAway(100)
                .scoreHome(home)
                .scoreAway(away)
                .winnerHome(winnerHome)
                .winnerAway(winnerAway)
                .build();
    }

    private Slip buildSlip(int teamId) {
        return Slip.builder()
                .betSlipId(1L)
                .teamId(teamId)
                .build();
    }

    @Test
    void testHomeTeamWins() {
        Slip slip = buildSlip(200);
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testHomeTeamLoses() {
        Slip slip = buildSlip(200);
        Score score = buildScore(1, 2, 0, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testAwayTeamWins() {
        Slip slip = buildSlip(100);
        Score score = buildScore(1, 2, 0, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testAwayTeamLoses() {
        Slip slip = buildSlip(100);
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testDrawPickWins() {
        // teamId = 3 is the draw pick
        Slip slip = buildSlip(3);
        Score score = buildScore(1, 1, 0, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testDrawPickLosesWhenHomeWins() {
        Slip slip = buildSlip(3);
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testDrawPickLosesWhenAwayWins() {
        Slip slip = buildSlip(3);
        Score score = buildScore(1, 2, 0, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testReturnsVoidWhenTeamNotMatched() {
        // teamId 999 is not home or away team
        Slip slip = buildSlip(999);
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.VOID.getStatus(), result);
    }

    @Test
    void testHomeTeamPickOnAwayWin() {
        Slip slip = buildSlip(200);
        Score score = buildScore(0, 1, 0, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testAwayTeamPickOnHomeWin() {
        Slip slip = buildSlip(100);
        Score score = buildScore(1, 0, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }
}
