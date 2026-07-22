package com.bix.event_consumer.consumers;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.messaging.TransactionMessageHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionsConsumerTest {
    @Mock
    private TransactionMessageHandler transactionMessageHandler;

    @Test
    void delegatesTransactionUpdateToSharedHandler() {
        BetStatusUpdate update = BetStatusUpdate.builder().betId(1L).build();
        TransactionsConsumer consumer = new TransactionsConsumer(transactionMessageHandler);

        consumer.consume(update);

        verify(transactionMessageHandler).handle(update);
    }

    @Test
    void propagatesHandlerFailureForBrokerRetry() {
        BetStatusUpdate update = BetStatusUpdate.builder().betId(1L).build();
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(transactionMessageHandler).handle(update);
        TransactionsConsumer consumer = new TransactionsConsumer(transactionMessageHandler);

        assertThrows(RuntimeException.class, () -> consumer.consume(update));
    }
}
