package com.bix.event_consumer.services;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.producer.TransactionProducer;
import com.bix.event_consumer.repositories.TransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionProducer transactionProducer;
    private final TransactionsRepository transactionsRepository;

    // to be called in producer
    public void publishBetStatus(BetStatusUpdate betStatusUpdate) {
        this.transactionProducer.publish(betStatusUpdate);
    }

    // to be called in consumer
    public void consumeBetTransactions(BetStatusUpdate bet) {
        String createdBy = "TRANSACTION-SERVICE";
        this.transactionsRepository.addTransaction(bet,createdBy);
    }
}
