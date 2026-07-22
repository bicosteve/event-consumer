package com.bix.event_consumer.messaging;

public interface ResultTriggerPublisher {
    void publish(String eventId);
}
