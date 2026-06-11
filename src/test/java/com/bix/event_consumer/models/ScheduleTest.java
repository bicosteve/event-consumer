package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScheduleTest {

    @Test
    void testNoArgsConstructor() {
        Schedule schedule = new Schedule();
        assertNotNull(schedule);
        assertNull(schedule.getSeasonYear());
    }

    @Test
    void testAllArgsConstructor() {
        Schedule schedule = new Schedule(true, "Regular Season", 2025,
                "Lakers vs Celtics", "Big Match", "20000");

        assertEquals(true, schedule.getConferenceCompetition());
        assertEquals("Regular Season", schedule.getSeasonType());
        assertEquals(2025, schedule.getSeasonYear());
        assertEquals("Lakers vs Celtics", schedule.getEventName());
        assertEquals("Big Match", schedule.getEventHeadline());
        assertEquals("20000", schedule.getAttendance());
    }

    @Test
    void testBuilder() {
        Schedule schedule = Schedule.builder()
                .eventName("Lakers vs Celtics")
                .seasonYear(2025)
                .build();

        assertEquals("Lakers vs Celtics", schedule.getEventName());
        assertEquals(2025, schedule.getSeasonYear());
    }

    @Test
    void testSettersAndGetters() {
        Schedule schedule = new Schedule();
        schedule.setConferenceCompetition(true);
        schedule.setSeasonType("Regular Season");
        schedule.setSeasonYear(2025);
        schedule.setEventName("Lakers vs Celtics");
        schedule.setEventHeadline("Big Match");
        schedule.setAttendance("20000");

        assertEquals(true, schedule.getConferenceCompetition());
        assertEquals("Regular Season", schedule.getSeasonType());
        assertEquals(2025, schedule.getSeasonYear());
        assertEquals("Lakers vs Celtics", schedule.getEventName());
        assertEquals("Big Match", schedule.getEventHeadline());
        assertEquals("20000", schedule.getAttendance());
    }
}
