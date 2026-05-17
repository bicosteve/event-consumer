package com.bix.event_consumer.services;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.producer.TransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionProducer transactionProducer;

    public void publishBetStatus(BetStatusUpdate betStatusUpdate) {
        this.transactionProducer.publish(betStatusUpdate);
    }
}
