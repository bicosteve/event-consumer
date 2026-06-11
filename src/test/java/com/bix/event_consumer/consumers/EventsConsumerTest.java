package com.bix.event_consumer.consumers;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import com.bix.event_consumer.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventsConsumerTest {

    @Mock
    private EventService eventService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQConfig rabbitMQConfig;

    @Mock
    private RabbitMQConfig.QueueConfig resultsConfig;

    private EventsConsumer eventsConsumer;

    @BeforeEach
    void setUp() {
        eventsConsumer = new EventsConsumer(eventService, rabbitTemplate, rabbitMQConfig);
    }

    private Event buildEvent(EventStatus status) {
        return Event.builder()
                .eventId("evt-1")
                .eventUuid("uuid-1")
                .score(Score.builder().eventStatus(status).build())
                .build();
    }

    @Test
    void testConsumeCallsEventService() {
        Event event = buildEvent(null);

        eventsConsumer.consume(event);

        verify(eventService, times(1)).consumeEvents(event);
    }

    @Test
    void testConsumePublishesToResultsQueueWhenStatusIsFinal() {
        Event event = buildEvent(EventStatus.STATUS_FINAL);

        when(rabbitMQConfig.getResults()).thenReturn(resultsConfig);
        when(resultsConfig.getExchange()).thenReturn("results.exchange");
        when(resultsConfig.getRoutingKey()).thenReturn("results.key");

        eventsConsumer.consume(event);

        verify(eventService, times(1)).consumeEvents(event);
        verify(rabbitTemplate, times(1))
                .convertAndSend("results.exchange", "results.key", "evt-1");
    }

    @Test
    void testConsumePublishesToResultsQueueWhenStatusIsFullTime() {
        Event event = buildEvent(EventStatus.STATUS_FULL_TIME);

        when(rabbitMQConfig.getResults()).thenReturn(resultsConfig);
        when(resultsConfig.getExchange()).thenReturn("results.exchange");
        when(resultsConfig.getRoutingKey()).thenReturn("results.key");

        eventsConsumer.consume(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testConsumePublishesToResultsQueueWhenStatusIsFinalPen() {
        Event event = buildEvent(EventStatus.STATUS_FINAL_PEN);

        when(rabbitMQConfig.getResults()).thenReturn(resultsConfig);
        when(resultsConfig.getExchange()).thenReturn("results.exchange");
        when(resultsConfig.getRoutingKey()).thenReturn("results.key");

        eventsConsumer.consume(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testConsumePublishesToResultsQueueWhenStatusIsFinalAet() {
        Event event = buildEvent(EventStatus.STATUS_FINAL_AET);

        when(rabbitMQConfig.getResults()).thenReturn(resultsConfig);
        when(resultsConfig.getExchange()).thenReturn("results.exchange");
        when(resultsConfig.getRoutingKey()).thenReturn("results.key");

        eventsConsumer.consume(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testConsumeDoesNotPublishWhenStatusIsInProgress() {
        Event event = buildEvent(EventStatus.STATUS_IN_PROGRESS);

        eventsConsumer.consume(event);

        verify(eventService, times(1)).consumeEvents(event);
        verify(rabbitTemplate, never())
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testConsumeDoesNotPublishWhenStatusIsScheduled() {
        Event event = buildEvent(EventStatus.STATUS_SCHEDULED);

        eventsConsumer.consume(event);

        verify(rabbitTemplate, never())
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testConsumeDoesNotPublishWhenScoreIsNull() {
        Event event = Event.builder()
                .eventId("evt-1")
                .score(null)
                .build();

        eventsConsumer.consume(event);

        verify(rabbitTemplate, never())
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testConsumeCatchesExceptionFromEventService() {
        Event event = buildEvent(EventStatus.STATUS_FINAL);

        doThrow(new RuntimeException("oops"))
                .when(eventService).consumeEvents(event);

        // Should not throw; exception is caught
        eventsConsumer.consume(event);

        verify(eventService, times(1)).consumeEvents(event);
    }

    @Test
    void testConsumeCatchesExceptionFromRabbitTemplate() {
        Event event = buildEvent(EventStatus.STATUS_FINAL);

        doThrow(new RuntimeException("rabbit down"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(Object.class));
        when(rabbitMQConfig.getResults()).thenReturn(resultsConfig);
        when(resultsConfig.getExchange()).thenReturn("results.exchange");
        when(resultsConfig.getRoutingKey()).thenReturn("results.key");

        // Should not throw
        eventsConsumer.consume(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
