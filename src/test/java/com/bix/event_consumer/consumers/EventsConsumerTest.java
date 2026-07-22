package com.bix.event_consumer.consumers;

import com.bix.event_consumer.messaging.EventMessageHandler;
import com.bix.event_consumer.models.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventsConsumerTest {
    @Mock
    private EventMessageHandler eventMessageHandler;

    @Test
    void delegatesIncomingEventToSharedHandler() {
        Event event = Event.builder().eventId("evt-1").build();
        EventsConsumer consumer = new EventsConsumer(eventMessageHandler);

        consumer.consume(event);

        verify(eventMessageHandler).handle(event);
    }

    @Test
    void propagatesHandlerFailureForBrokerRetry() {
        Event event = Event.builder().eventId("evt-1").build();
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(eventMessageHandler).handle(event);
        EventsConsumer consumer = new EventsConsumer(eventMessageHandler);

        assertThrows(RuntimeException.class, () -> consumer.consume(event));
    }
}
