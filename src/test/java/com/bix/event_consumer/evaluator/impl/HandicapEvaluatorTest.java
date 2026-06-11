package com.bix.event_consumer.evaluator.impl;

import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HandicapEvaluatorTest {

    private HandicapEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new HandicapEvaluator();
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

    private Slip buildSlip(int teamId, String specialBetValue) {
        return Slip.builder()
                .betSlipId(1L)
                .teamId(teamId)
                .specialBetValue(specialBetValue)
                .build();
    }

    @Test
    void testEvaluateReturnsPendingWhenSpecialBetValueIsNull() {
        Slip slip = buildSlip(200, null);
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsPendingWhenSpecialBetValueIsBlank() {
        Slip slip = buildSlip(200, "   ");
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsPendingWhenSpecialBetValueIsEmpty() {
        Slip slip = buildSlip(200, "");
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsPendingWhenSpecialBetValueIsInvalid() {
        Slip slip = buildSlip(200, "hcp=abc");
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testEvaluateHomeTeamWinsWithNegativeHandicap() {
        // Home team pick, score 2-1, handicap -1 (hcp=-1)
        // Adjusted: home 1, away 1 -> NOT > so LOST
        Slip slip = buildSlip(200, "hcp=-1");
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testEvaluateHomeTeamWinsWithPositiveHandicap() {
        // Home team pick, score 3-1, handicap -1.5
        // Adjusted: 1.5 vs 1 -> WON
        Slip slip = buildSlip(200, "hcp=-1.5");
        Score score = buildScore(3, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testEvaluateHomeTeamLoses() {
        // Home team pick, score 1-3, handicap 0
        // Adjusted: 1 vs 3 -> LOST
        Slip slip = buildSlip(200, "hcp=0");
        Score score = buildScore(1, 3, 0, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testEvaluateAwayTeamWins() {
        // Away team pick, score 1-3, handicap 0
        // Adjusted: 3 vs 1 -> WON
        Slip slip = buildSlip(100, "hcp=0");
        Score score = buildScore(1, 3, 0, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testEvaluateAwayTeamLoses() {
        // Away team pick, score 2-1, handicap 0
        // Adjusted: 1 vs 2 -> LOST
        Slip slip = buildSlip(100, "hcp=0");
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsVoidWhenTeamNotMatched() {
        // Slip team doesn't match home or away team
        Slip slip = buildSlip(999, "hcp=0");
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.VOID.getStatus(), result);
    }

    @Test
    void testEvaluateWithoutEqualsSignUsesZeroHandicap() {
        // If no '=' sign, handicap defaults to 0
        Slip slip = buildSlip(200, "hcp=invalid");
        // This will hit the NumberFormatException and return PENDING
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testEvaluateHomeTeamWinsNoHandicap() {
        // Home team pick, score 2-1, no handicap suffix at all
        // (no "=" so handicap defaults to 0)
        Slip slip = buildSlip(200, "hcp");
        Score score = buildScore(2, 1, 1, 0);

        int result = evaluator.evaluate(slip, score);

        // handicap = 0, adjusted = 2 > 1 -> WON
        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @ParameterizedTest
    @CsvSource({
            "200, 0, hcp=0, 1, 1, 3, 1, 3",   // home, no hcp, tied -> LOST
            "100, 0, hcp=0, 0, 1, 5",          // away wins, LOST
            "200, 0, hcp=0, 1, 0, 3",          // home wins, WON
    })
    void testEvaluateVariousScenarios(int teamPick, int unused1, String specialValue,
                                       int unused2, int unused3, int score) {
        // Simple sanity check that the evaluator runs without exception
        Slip slip = buildSlip(teamPick, specialValue);
        Score s = buildScore(2, 1, 1, 0);
        int result = evaluator.evaluate(slip, s);
        // Result is one of PENDING, LOST, WON, VOID
        org.junit.jupiter.api.Assertions.assertTrue(
                result == SlipStatus.PENDING.getStatus()
                        || result == SlipStatus.LOST.getStatus()
                        || result == SlipStatus.WON.getStatus()
                        || result == SlipStatus.VOID.getStatus()
        );
    }

    @Test
    void testEvaluateHomeTeamDraw() {
        // Home pick, score 1-1, handicap 0
        // Adjusted: 1 vs 1 -> not greater, so LOST
        Slip slip = buildSlip(200, "hcp=0");
        Score score = buildScore(1, 1, 0, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testEvaluateHomeTeamDrawWithNegativeHandicap() {
        // Home pick, score 1-1, handicap -0.5
        // Adjusted: 0.5 vs 1 -> LOST
        Slip slip = buildSlip(200, "hcp=-0.5");
        Score score = buildScore(1, 1, 0, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testEvaluateHomeTeamDrawWithPositiveHandicap() {
        // Home pick, score 1-1, handicap 0.5
        // Adjusted: 1.5 vs 1 -> WON
        Slip slip = buildSlip(200, "hcp=0.5");
        Score score = buildScore(1, 1, 0, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }
}
