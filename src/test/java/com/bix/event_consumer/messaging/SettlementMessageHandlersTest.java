package com.bix.event_consumer.messaging;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.services.ResultService;
import com.bix.event_consumer.services.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementMessageHandlersTest {

    @Mock
    private ResultService resultService;

    @Mock
    private TransactionService transactionService;

    @Test
    void resultHandlerDelegatesSettlementToResultService() {
        ResultMessageHandler handler = new ResultMessageHandler(resultService);

        handler.handle("event-1");

        verify(resultService).processBetResults("event-1");
    }

    @Test
    void transactionHandlerDelegatesUpdatesToTransactionService() {
        BetStatusUpdate update = BetStatusUpdate.builder().betId(7L).build();
        TransactionMessageHandler handler = new TransactionMessageHandler(transactionService);

        handler.handle(update);

        verify(transactionService).consumeBetTransactions(update);
    }
}
