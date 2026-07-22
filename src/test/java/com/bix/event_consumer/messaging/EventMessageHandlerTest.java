package com.bix.event_consumer.messaging;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.services.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EventMessageHandlerTest {

    @Mock
    private EventService eventService;

    @Mock
    private ResultTriggerPublisher resultTriggerPublisher;

    @Test
    void handlesFinalEventAndPublishesItsEventIdForSettlement() {
        Event event = event(EventStatus.STATUS_FINAL);
        EventMessageHandler handler = new EventMessageHandler(eventService, resultTriggerPublisher);

        handler.handle(event);

        verify(eventService).consumeEvents(event);
        ArgumentCaptor<String> eventId = ArgumentCaptor.forClass(String.class);
        verify(resultTriggerPublisher).publish(eventId.capture());
        org.junit.jupiter.api.Assertions.assertEquals("event-1", eventId.getValue());
    }

 @ParameterizedTest
 @EnumSource(value = EventStatus.class, names = {
 "STATUS_FINAL", "STATUS_FULL_TIME", "STATUS_FINAL_PEN", "STATUS_FINAL_AET",
 "STATUS_POSTPONED", "STATUS_CANCELED", "STATUS_SUSPENDED", "STATUS_FORFEIT",
 "STATUS_RETIRED", "STATUS_UNKNOWN"
 })
 void publishesSettlementTriggerForEveryTerminalOrVoidStatus(EventStatus status) {
 Event event = event(status);
 EventMessageHandler handler = new EventMessageHandler(eventService, resultTriggerPublisher);

 handler.handle(event);

 verify(eventService).consumeEvents(event);
 verify(resultTriggerPublisher).publish("event-1");
 }

 @Test
 void doesNotPublishSettlementTriggerForInProgressEvent() {
        Event event = event(EventStatus.STATUS_IN_PROGRESS);
        EventMessageHandler handler = new EventMessageHandler(eventService, resultTriggerPublisher);

        handler.handle(event);

        verify(eventService).consumeEvents(event);
        verifyNoInteractions(resultTriggerPublisher);
    }

    @Test
    void propagatesServiceFailuresSoTheTransportCanRetry() {
        Event event = event(EventStatus.STATUS_FINAL);
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(eventService).consumeEvents(event);
        EventMessageHandler handler = new EventMessageHandler(eventService, resultTriggerPublisher);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> handler.handle(event));
        verifyNoInteractions(resultTriggerPublisher);
    }

    private Event event(EventStatus status) {
        return Event.builder()
                .eventId("event-1")
                .score(Score.builder().eventStatus(status).build())
                .build();
    }
}
