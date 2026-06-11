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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private BetSlipRepository betSlipRepository;

    @Mock
    private BetRepository betRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private MarketEvaluator moneylineEvaluator;

    @Mock
    private MarketEvaluator handicapEvaluator;

    private ResultService resultService;

    @BeforeEach
    void setUp() {
        Map<String, MarketEvaluator> evaluators = new HashMap<>();
        evaluators.put("moneylineEvaluator", moneylineEvaluator);
        evaluators.put("handicapEvaluator", handicapEvaluator);
        resultService = new ResultService(
                scoreRepository,
                betSlipRepository,
                betRepository,
                transactionService,
                evaluators
        );
    }

    private Score buildScore(EventStatus status) {
        return Score.builder()
                .eventId("evt-1")
                .eventStatus(status)
                .scoreHome(2)
                .scoreAway(1)
                .build();
    }

    private Slip buildSlip(Long betSlipId, Long betId, int status, String marketName) {
        return Slip.builder()
                .betSlipId(betSlipId)
                .betId(betId)
                .status(status)
                .marketName(marketName)
                .build();
    }

    @Test
    void testProcessBetResultsReturnsEarlyWhenScoreIsNull() {
        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(null);

        resultService.processBetResults("evt-1");

        verify(betSlipRepository, never()).findEventsPendingSlips(anyString());
        verify(transactionService, never()).publishBetStatus(any());
    }

    @Test
    void testProcessBetResultsReturnsEarlyWhenNoPendingSlips() {
        when(scoreRepository.findScoreByEventId("evt-1"))
                .thenReturn(buildScore(EventStatus.STATUS_FINAL));
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.emptyList());

        resultService.processBetResults("evt-1");

        verify(betSlipRepository, never()).updateSlipStatus(anyLong(), anyInt());
        verify(betRepository, never()).updateBetStatus(anyLong(), anyInt());
        verify(transactionService, never()).publishBetStatus(any());
    }

    @Test
    void testProcessBetResultsEvaluatesAndUpdatesSlips() {
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "moneyline");
        Slip slip2 = buildSlip(11L, 1L, SlipStatus.PENDING.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Arrays.asList(slip1, slip2));
        when(moneylineEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Arrays.asList(slip1, slip2));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().betId(1L).build());

        resultService.processBetResults("evt-1");

        verify(betSlipRepository, times(2)).updateSlipStatus(anyLong(), anyInt());
        verify(betRepository, times(1)).updateBetStatus(anyLong(), anyInt());
        verify(transactionService, times(1)).publishBetStatus(any());
    }

    @Test
    void testProcessBetResultsHandlesVoidedEventStatus() {
        Score score = buildScore(EventStatus.STATUS_CANCELED);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(slip1));
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Collections.singletonList(slip1));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().betId(1L).build());

        resultService.processBetResults("evt-1");

        // Slip is voided, so we do not call evaluator
        verify(moneylineEvaluator, never()).evaluate(any(), any());
        // Slip status is updated to VOID
        verify(betSlipRepository, times(1))
                .updateSlipStatus(10L, SlipStatus.VOID.getStatus());
    }

    @Test
    void testProcessBetResultsHandlesPostponedEventStatus() {
        Score score = buildScore(EventStatus.STATUS_POSTPONED);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(slip1));

        resultService.processBetResults("evt-1");

        verify(moneylineEvaluator, never()).evaluate(any(), any());
    }

    @Test
    void testProcessBetResultsHandlesSuspendedEventStatus() {
        Score score = buildScore(EventStatus.STATUS_SUSPENDED);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(slip1));

        resultService.processBetResults("evt-1");

        verify(moneylineEvaluator, never()).evaluate(any(), any());
    }

    @Test
    void testProcessBetResultsHandlesForfeitedEventStatus() {
        Score score = buildScore(EventStatus.STATUS_FORFEIT);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(slip1));

        resultService.processBetResults("evt-1");

        verify(moneylineEvaluator, never()).evaluate(any(), any());
    }

    @Test
    void testProcessBetResultsReturnsPendingWhenNoEvaluatorFound() {
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "unknown_market");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(slip1));

        resultService.processBetResults("evt-1");

        verify(betSlipRepository, times(1))
                .updateSlipStatus(10L, SlipStatus.PENDING.getStatus());
    }

    @Test
    void testProcessBetResultsUsesLowercaseMarketNameForEvaluator() {
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "MONEYLINE");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(slip1));
        when(moneylineEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Collections.singletonList(slip1));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().betId(1L).build());

        resultService.processBetResults("evt-1");

        verify(moneylineEvaluator, times(1)).evaluate(any(), any());
    }

    @Test
    void testProcessBetResultsGroupsSlipsByBet() {
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip slip1 = buildSlip(10L, 1L, SlipStatus.PENDING.getStatus(), "moneyline");
        Slip slip2 = buildSlip(11L, 2L, SlipStatus.PENDING.getStatus(), "handicap");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Arrays.asList(slip1, slip2));
        when(moneylineEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(handicapEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Collections.singletonList(slip1));
        when(betSlipRepository.findBetsSlip(2L))
                .thenReturn(Collections.singletonList(slip2));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().build());

        resultService.processBetResults("evt-1");

        // Two distinct bets => two status updates
        verify(betRepository, times(2)).updateBetStatus(anyLong(), anyInt());
        verify(transactionService, times(2)).publishBetStatus(any());
    }

    @Test
    void testProcessBetResultsCheckBetStatusRuleAnyLost() {
        // RULE 01: If any slip is LOST, the whole bet is LOST
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip lostSlip = buildSlip(10L, 1L, BetStatus.LOST.getStatus(), "moneyline");
        Slip wonSlip = buildSlip(11L, 1L, BetStatus.WON.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(wonSlip));
        when(moneylineEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Arrays.asList(lostSlip, wonSlip));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().build());

        resultService.processBetResults("evt-1");

        // Bet should be marked LOST
        verify(betRepository, times(1))
                .updateBetStatus(1L, BetStatus.LOST.getStatus());
    }

    @Test
    void testProcessBetResultsCheckBetStatusRuleAllVoid() {
        // RULE 03: If all slips are VOID, the bet is VOID
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip voidSlip = buildSlip(10L, 1L, BetStatus.VOID.getStatus(), "moneyline");
        Slip wonSlip = buildSlip(11L, 1L, BetStatus.WON.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(wonSlip));
        when(moneylineEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Arrays.asList(voidSlip, voidSlip));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().build());

        resultService.processBetResults("evt-1");

        verify(betRepository, times(1))
                .updateBetStatus(1L, BetStatus.VOID.getStatus());
    }

    @Test
    void testProcessBetResultsCheckBetStatusRulePending() {
        // RULE 02: If any slip is PENDING, the bet is PENDING
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip pendingSlip = buildSlip(10L, 1L, BetStatus.PENDING.getStatus(), "moneyline");
        Slip wonSlip = buildSlip(11L, 1L, BetStatus.WON.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(wonSlip));
        when(moneylineEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Arrays.asList(pendingSlip, wonSlip));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().build());

        resultService.processBetResults("evt-1");

        verify(betRepository, times(1))
                .updateBetStatus(1L, BetStatus.PENDING.getStatus());
    }

    @Test
    void testProcessBetResultsCheckBetStatusRuleMixWonAndVoid() {
        // RULE 04: Mix of WON + VOID = WON
        Score score = buildScore(EventStatus.STATUS_FINAL);
        Slip wonSlip = buildSlip(10L, 1L, BetStatus.WON.getStatus(), "moneyline");

        when(scoreRepository.findScoreByEventId("evt-1")).thenReturn(score);
        when(betSlipRepository.findEventsPendingSlips("evt-1"))
                .thenReturn(Collections.singletonList(wonSlip));
        when(moneylineEvaluator.evaluate(any(), any()))
                .thenReturn(SlipStatus.WON.getStatus());
        when(betSlipRepository.findBetsSlip(1L))
                .thenReturn(Arrays.asList(
                        Slip.builder().betSlipId(10L).status(BetStatus.WON.getStatus()).build(),
                        Slip.builder().betSlipId(11L).status(BetStatus.VOID.getStatus()).build()
                ));
        when(betRepository.updateBetStatus(anyLong(), anyInt()))
                .thenReturn(BetStatusUpdate.builder().build());

        resultService.processBetResults("evt-1");

        verify(betRepository, times(1))
                .updateBetStatus(1L, BetStatus.WON.getStatus());
    }
}
