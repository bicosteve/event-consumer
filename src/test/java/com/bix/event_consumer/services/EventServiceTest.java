package com.bix.event_consumer.services;

import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.repositories.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void testConsumeEventsWithNullDoesNotCallRepository() {
        eventService.consumeEvents(null);

        verify(eventRepository, never()).updateEvent(null);
    }

    @Test
    void testConsumeEventsWithValidEventUpdatesRepository() {
        Event event = Event.builder()
                .eventId("evt-1")
                .eventUuid("uuid-1")
                .build();

        eventService.consumeEvents(event);

        verify(eventRepository, times(1)).updateEvent(event);
    }
}
