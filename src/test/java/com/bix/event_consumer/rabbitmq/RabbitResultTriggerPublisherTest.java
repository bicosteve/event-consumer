package com.bix.event_consumer.rabbitmq;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RabbitResultTriggerPublisherTest {
    @Test
    void mapsEventIdToConfiguredResultsRoute() {
        ConfirmingRabbitPublisher confirmingPublisher = mock(ConfirmingRabbitPublisher.class);
        RabbitMQConfig config = mock(RabbitMQConfig.class);
        RabbitMQConfig.QueueConfig results = mock(RabbitMQConfig.QueueConfig.class);
        when(config.getResults()).thenReturn(results);
        when(results.getExchange()).thenReturn("results.exchange");
        when(results.getRoutingKey()).thenReturn("results.key");

        new RabbitResultTriggerPublisher(confirmingPublisher, config).publish("event-42");

        verify(confirmingPublisher).publish("results.exchange", "results.key", "event-42");
    }
}
