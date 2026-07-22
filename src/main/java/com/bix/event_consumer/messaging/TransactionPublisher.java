package com.bix.event_consumer.messaging;

import com.bix.event_consumer.events.BetStatusUpdate;

public interface TransactionPublisher {
    void publish(BetStatusUpdate event);
}
