package com.bix.event_consumer.services;

import com.bix.event_consumer.enums.BetStatus;
import com.bix.event_consumer.enums.TransactionType;
import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.producer.TransactionProducer;
import com.bix.event_consumer.repositories.TransactionsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionProducer transactionProducer;

    @Mock
    private TransactionsRepository transactionsRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testPublishBetStatusCallsProducer() {
        BetStatusUpdate update = BetStatusUpdate.builder()
                .betId(1L)
                .build();

        transactionService.publishBetStatus(update);

        verify(transactionProducer, times(1)).publish(update);
    }

    @Test
    void testConsumeBetTransactionsWonStatusSetsTypeToCredit() {
        BetStatusUpdate bet = BetStatusUpdate.builder()
                .betId(1L)
                .profileId(2L)
                .currentStatus(BetStatus.WON.getStatus())
                .build();

        transactionService.consumeBetTransactions(bet);

        assertEquals(TransactionType.CREDIT.getStatus(), bet.getType());
        verify(transactionsRepository, times(1)).addTransaction(bet, "TRX-SERVICE");
    }

    @Test
    void testConsumeBetTransactionsVoidStatusSetsTypeToRefund() {
        BetStatusUpdate bet = BetStatusUpdate.builder()
                .betId(1L)
                .profileId(2L)
                .currentStatus(BetStatus.VOID.getStatus())
                .build();

        transactionService.consumeBetTransactions(bet);

        assertEquals(TransactionType.REFUND.getStatus(), bet.getType());
        verify(transactionsRepository, times(1)).addTransaction(bet, "TRX-SERVICE");
    }

    @Test
    void testConsumeBetTransactionsPendingStatusDoesNotAddTransaction() {
        BetStatusUpdate bet = BetStatusUpdate.builder()
                .betId(1L)
                .profileId(2L)
                .currentStatus(BetStatus.PENDING.getStatus())
                .build();

        transactionService.consumeBetTransactions(bet);

        verify(transactionsRepository, never()).addTransaction(bet, "TRX-SERVICE");
    }

    @Test
    void testConsumeBetTransactionsLostStatusDoesNotAddTransaction() {
        BetStatusUpdate bet = BetStatusUpdate.builder()
                .betId(1L)
                .profileId(2L)
                .currentStatus(BetStatus.LOST.getStatus())
                .build();

        transactionService.consumeBetTransactions(bet);

        verify(transactionsRepository, never()).addTransaction(bet, "TRX-SERVICE");
    }

    @Test
    void testConsumeBetTransactionsUsesArgumentCaptor() {
        BetStatusUpdate bet = BetStatusUpdate.builder()
                .betId(1L)
                .profileId(2L)
                .currentStatus(BetStatus.WON.getStatus())
                .build();

        transactionService.consumeBetTransactions(bet);

        ArgumentCaptor<BetStatusUpdate> captor = ArgumentCaptor.forClass(BetStatusUpdate.class);
        ArgumentCaptor<String> createdByCaptor = ArgumentCaptor.forClass(String.class);
        verify(transactionsRepository).addTransaction(captor.capture(), createdByCaptor.capture());
        assertEquals(BetStatus.WON.getStatus(), bet.getCurrentStatus());
        assertEquals(TransactionType.CREDIT.getStatus(), bet.getType());
        assertEquals("TRX-SERVICE", createdByCaptor.getValue());
    }
}
