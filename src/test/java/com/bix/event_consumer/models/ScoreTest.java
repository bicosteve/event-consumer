package com.bix.event_consumer.models;

import com.bix.event_consumer.enums.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScoreTest {

    @Test
    void testNoArgsConstructor() {
        Score score = new Score();
        assertNotNull(score);
        assertNull(score.getScoreId());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Score score = new Score(1L, "evt-1", EventStatus.STATUS_FINAL, "Final",
                100, 200, 0, 1, 3, 1, 0, 4, "ESPN", "Stadium", "City",
                now, now);

        assertEquals(1L, score.getScoreId());
        assertEquals("evt-1", score.getEventId());
        assertEquals(EventStatus.STATUS_FINAL, score.getEventStatus());
        assertEquals("Final", score.getEventStatusDetail());
        assertEquals(100, score.getTeamIdAway());
        assertEquals(200, score.getTeamIdHome());
        assertEquals(0, score.getWinnerAway());
        assertEquals(1, score.getWinnerHome());
        assertEquals(3, score.getScoreAway());
        assertEquals(1, score.getScoreHome());
        assertEquals(0, score.getGameClock());
        assertEquals(4, score.getGamePeriod());
        assertEquals("ESPN", score.getBroadcast());
        assertEquals("Stadium", score.getVenueName());
        assertEquals("City", score.getVenueLocation());
    }

    @Test
    void testBuilder() {
        Score score = Score.builder()
                .eventId("evt-1")
                .eventStatus(EventStatus.STATUS_FINAL)
                .scoreHome(3)
                .scoreAway(1)
                .build();

        assertEquals("evt-1", score.getEventId());
        assertEquals(EventStatus.STATUS_FINAL, score.getEventStatus());
        assertEquals(3, score.getScoreHome());
        assertEquals(1, score.getScoreAway());
    }

    @Test
    void testSettersAndGetters() {
        Score score = new Score();
        score.setScoreId(1L);
        score.setEventId("evt-1");
        score.setEventStatus(EventStatus.STATUS_FINAL);
        score.setEventStatusDetail("Final");
        score.setTeamIdAway(100);
        score.setTeamIdHome(200);
        score.setWinnerAway(0);
        score.setWinnerHome(1);
        score.setScoreAway(3);
        score.setScoreHome(1);
        score.setGameClock(0);
        score.setGamePeriod(4);
        score.setBroadcast("ESPN");
        score.setVenueName("Stadium");
        score.setVenueLocation("City");

        assertEquals(1L, score.getScoreId());
        assertEquals("evt-1", score.getEventId());
        assertEquals(EventStatus.STATUS_FINAL, score.getEventStatus());
        assertEquals(100, score.getTeamIdAway());
        assertEquals(200, score.getTeamIdHome());
        assertEquals(0, score.getWinnerAway());
        assertEquals(1, score.getWinnerHome());
        assertEquals(3, score.getScoreAway());
        assertEquals(1, score.getScoreHome());
        assertEquals(0, score.getGameClock());
        assertEquals(4, score.getGamePeriod());
        assertEquals("ESPN", score.getBroadcast());
        assertEquals("Stadium", score.getVenueName());
        assertEquals("City", score.getVenueLocation());
    }
}
