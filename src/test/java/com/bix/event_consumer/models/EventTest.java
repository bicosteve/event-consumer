package com.bix.event_consumer.models;

import com.bix.event_consumer.enums.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventTest {

    @Test
    void testNoArgsConstructor() {
        Event event = new Event();
        assertNotNull(event);
        assertNull(event.getEventId());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Team> teams = new ArrayList<>();
        List<Market> markets = new ArrayList<>();
        Score score = new Score();
        Schedule schedule = new Schedule();

        Event event = new Event(1, "evt-1", "uuid-1", 1, now, teams, markets, score, schedule, now, now, EventStatus.STATUS_FINAL);

        assertEquals(1, event.getId());
        assertEquals("evt-1", event.getEventId());
        assertEquals("uuid-1", event.getEventUuid());
        assertEquals(1, event.getSportId());
        assertEquals(now, event.getEventDate());
        assertEquals(teams, event.getTeams());
        assertEquals(markets, event.getMarkets());
        assertEquals(score, event.getScore());
        assertEquals(schedule, event.getSchedule());
        assertEquals(EventStatus.STATUS_FINAL, event.getStatus());
    }

    @Test
    void testBuilder() {
        Event event = Event.builder()
                .eventId("evt-1")
                .eventUuid("uuid-1")
                .sportId(1)
                .status(EventStatus.STATUS_FINAL)
                .build();

        assertEquals("evt-1", event.getEventId());
        assertEquals("uuid-1", event.getEventUuid());
        assertEquals(1, event.getSportId());
        assertEquals(EventStatus.STATUS_FINAL, event.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        Event event = new Event();
        event.setId(1);
        event.setEventId("evt-1");
        event.setEventUuid("uuid-1");
        event.setSportId(2);
        event.setStatus(EventStatus.STATUS_IN_PROGRESS);

        assertEquals(1, event.getId());
        assertEquals("evt-1", event.getEventId());
        assertEquals("uuid-1", event.getEventUuid());
        assertEquals(2, event.getSportId());
        assertEquals(EventStatus.STATUS_IN_PROGRESS, event.getStatus());
    }
}
