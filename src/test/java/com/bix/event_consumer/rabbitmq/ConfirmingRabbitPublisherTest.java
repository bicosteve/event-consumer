package com.bix.event_consumer.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class ConfirmingRabbitPublisherTest {
    @Test
    void returnsAfterBrokerAck() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        doAnswer(invocation -> {
            CorrelationData data = invocation.getArgument(3);
            data.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(template).convertAndSend(eq("exchange"), eq("route"), eq("payload"), any(CorrelationData.class));

        assertDoesNotThrow(() -> new ConfirmingRabbitPublisher(template, Duration.ofSeconds(1))
            .publish("exchange", "route", "payload"));
    }

    @Test
    void throwsWhenBrokerNacksPublication() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        doAnswer(invocation -> {
            CorrelationData data = invocation.getArgument(3);
            data.getFuture().complete(new CorrelationData.Confirm(false, "rejected"));
            return null;
        }).when(template).convertAndSend(eq("exchange"), eq("route"), eq("payload"), any(CorrelationData.class));

        assertThrows(RabbitPublishException.class, () -> new ConfirmingRabbitPublisher(template, Duration.ofSeconds(1))
            .publish("exchange", "route", "payload"));
    }

    @Test
    void throwsWhenPublicationIsReturned() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        doAnswer(invocation -> {
            CorrelationData data = invocation.getArgument(3);
            data.setReturned(mock(org.springframework.amqp.core.ReturnedMessage.class));
            data.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(template).convertAndSend(eq("exchange"), eq("route"), eq("payload"), any(CorrelationData.class));

        assertThrows(RabbitPublishException.class, () -> new ConfirmingRabbitPublisher(template, Duration.ofSeconds(1))
            .publish("exchange", "route", "payload"));
    }

    @Test
    void throwsWhenConfirmTimesOut() {
        RabbitTemplate template = mock(RabbitTemplate.class);

        assertThrows(RabbitPublishException.class, () -> new ConfirmingRabbitPublisher(template, Duration.ofMillis(1))
            .publish("exchange", "route", "payload"));
    }
}
