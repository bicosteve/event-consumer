package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamTest {

    @Test
    void testNoArgsConstructor() {
        Team team = new Team();
        assertNotNull(team);
        assertNull(team.getId());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Conference conference = new Conference();
        Team team = new Team(1, 100, "evt-1", "Lakers", "LAL", "LAL", 1, 1, 1,
                "10-5", false, true, conference, now, now);

        assertEquals(1, team.getId());
        assertEquals(100, team.getTeamId());
        assertEquals("evt-1", team.getEventId());
        assertEquals("Lakers", team.getName());
        assertEquals("LAL", team.getMascot());
        assertEquals("LAL", team.getAbbreviation());
        assertEquals(1, team.getConferenceId());
        assertEquals(1, team.getDivisionId());
        assertEquals(1, team.getRanking());
        assertEquals("10-5", team.getRecord());
        assertFalse(team.getIsAway());
        assertTrue(team.getIsHome());
        assertEquals(conference, team.getConference());
    }

    @Test
    void testBuilder() {
        Team team = Team.builder()
                .teamId(100)
                .name("Lakers")
                .abbreviation("LAL")
                .isHome(true)
                .isAway(false)
                .build();

        assertEquals(100, team.getTeamId());
        assertEquals("Lakers", team.getName());
        assertEquals("LAL", team.getAbbreviation());
        assertTrue(team.getIsHome());
        assertFalse(team.getIsAway());
    }

    @Test
    void testSettersAndGetters() {
        Team team = new Team();
        team.setId(1);
        team.setTeamId(100);
        team.setEventId("evt-1");
        team.setName("Lakers");
        team.setMascot("LAL");
        team.setAbbreviation("LAL");
        team.setConferenceId(1);
        team.setDivisionId(1);
        team.setRanking(1);
        team.setRecord("10-5");
        team.setIsAway(false);
        team.setIsHome(true);

        assertEquals(1, team.getId());
        assertEquals(100, team.getTeamId());
        assertEquals("evt-1", team.getEventId());
        assertEquals("Lakers", team.getName());
        assertEquals("LAL", team.getMascot());
        assertEquals("LAL", team.getAbbreviation());
        assertEquals(1, team.getConferenceId());
        assertEquals(1, team.getDivisionId());
        assertEquals(1, team.getRanking());
        assertEquals("10-5", team.getRecord());
        assertFalse(team.getIsAway());
        assertTrue(team.getIsHome());
    }
}
