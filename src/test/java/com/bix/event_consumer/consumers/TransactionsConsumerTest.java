package com.bix.event_consumer.consumers;

import com.bix.event_consumer.enums.BetStatus;
import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionsConsumerTest {

    @Mock
    private TransactionService transactionService;

    private TransactionsConsumer transactionsConsumer;

    @BeforeEach
    void setUp() {
        transactionsConsumer = new TransactionsConsumer(transactionService);
    }

    @Test
    void testConsumeCallsTransactionService() {
        BetStatusUpdate bet = BetStatusUpdate.builder()
                .betId(1L)
                .currentStatus(BetStatus.WON.getStatus())
                .build();

        transactionsConsumer.consume(bet);

        verify(transactionService, times(1)).consumeBetTransactions(bet);
    }

    @Test
    void testConsumeCatchesExceptionFromTransactionService() {
        BetStatusUpdate bet = BetStatusUpdate.builder()
                .betId(1L)
                .currentStatus(BetStatus.WON.getStatus())
                .build();

        doThrow(new RuntimeException("oops"))
                .when(transactionService).consumeBetTransactions(bet);

        // Should not throw
        transactionsConsumer.consume(bet);

        verify(transactionService, times(1)).consumeBetTransactions(bet);
    }
}
