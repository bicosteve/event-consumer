package com.bix.event_consumer.messaging;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionMessageHandler {
    private final TransactionService transactionService;

    public void handle(BetStatusUpdate update) {
        transactionService.consumeBetTransactions(update);
    }
}
