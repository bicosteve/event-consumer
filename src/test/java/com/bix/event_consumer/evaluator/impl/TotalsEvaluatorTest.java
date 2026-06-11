package com.bix.event_consumer.evaluator.impl;

import com.bix.event_consumer.enums.SlipStatus;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotalsEvaluatorTest {

    private TotalsEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new TotalsEvaluator();
    }

    private Score buildScore(int home, int away) {
        return Score.builder()
                .teamIdHome(200)
                .teamIdAway(100)
                .scoreHome(home)
                .scoreAway(away)
                .build();
    }

    private Slip buildSlip(int teamId, String participantName, String specialBetValue) {
        return Slip.builder()
                .betSlipId(1L)
                .teamId(teamId)
                .participantName(participantName)
                .specialBetValue(specialBetValue)
                .build();
    }

    @Test
    void testEvaluateReturnsPendingWhenSpecialBetValueIsNull() {
        Slip slip = buildSlip(9, "over", null);
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsPendingWhenSpecialBetValueIsBlank() {
        Slip slip = buildSlip(9, "over", "  ");
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsVoidWhenTeamIdIsInvalid() {
        Slip slip = buildSlip(5, "over", "hcp=2.5");
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.VOID.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsVoidWhenParticipantNameIsInvalid() {
        Slip slip = buildSlip(9, "invalid", "hcp=2.5");
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.VOID.getStatus(), result);
    }

    @Test
    void testEvaluateReturnsPendingWhenSpecialBetValueIsInvalid() {
        Slip slip = buildSlip(9, "over", "hcp=abc");
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.PENDING.getStatus(), result);
    }

    @Test
    void testOverWinsWhenTotalIsGreaterThanHandicap() {
        // Total 3, handicap 2.5 -> OVER wins
        Slip slip = buildSlip(9, "over", "hcp=2.5");
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testOverLosesWhenTotalIsLessThanHandicap() {
        // Total 2, handicap 2.5 -> OVER loses
        Slip slip = buildSlip(9, "over", "hcp=2.5");
        Score score = buildScore(1, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testUnderWinsWhenTotalIsLessThanHandicap() {
        // Total 2, handicap 2.5 -> UNDER wins
        Slip slip = buildSlip(10, "under", "hcp=2.5");
        Score score = buildScore(1, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testUnderLosesWhenTotalIsGreaterThanHandicap() {
        // Total 3, handicap 2.5 -> UNDER loses
        Slip slip = buildSlip(10, "under", "hcp=2.5");
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testOverLosesWhenTotalEqualsHandicap() {
        // Total 2.5, handicap 2.5 -> not greater, so LOST
        Slip slip = buildSlip(9, "over", "hcp=2.5");
        Score score = buildScore(1, 1); // total 2

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testOverWithZeroScore() {
        // Total 0, handicap 2.5 -> OVER loses
        Slip slip = buildSlip(9, "over", "hcp=2.5");
        Score score = buildScore(0, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.LOST.getStatus(), result);
    }

    @Test
    void testUnderWithZeroScore() {
        // Total 0, handicap 2.5 -> UNDER wins
        Slip slip = buildSlip(10, "under", "hcp=2.5");
        Score score = buildScore(0, 0);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testParticipantNameIsCaseInsensitive() {
        // Capital "OVER" should work
        Slip slip = buildSlip(9, "OVER", "hcp=2.5");
        Score score = buildScore(2, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }

    @Test
    void testParticipantNameMixedCaseUnder() {
        // Mixed case "Under" should work
        Slip slip = buildSlip(10, "Under", "hcp=2.5");
        Score score = buildScore(1, 1);

        int result = evaluator.evaluate(slip, score);

        assertEquals(SlipStatus.WON.getStatus(), result);
    }
}
