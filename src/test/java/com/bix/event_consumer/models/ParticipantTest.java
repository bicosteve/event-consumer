package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ParticipantTest {

    @Test
    void testNoArgsConstructor() {
        Participant participant = new Participant();
        assertNotNull(participant);
        assertNull(participant.getId());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Line> lines = new ArrayList<>();
        Participant participant = new Participant(1, 100, "team", "Lakers", 1L, lines, now, now);

        assertEquals(1, participant.getParticipantId());
        assertEquals(100, participant.getId());
        assertEquals("team", participant.getType());
        assertEquals("Lakers", participant.getName());
        assertEquals(1L, participant.getMarketId());
        assertEquals(lines, participant.getLines());
    }

    @Test
    void testBuilder() {
        Participant participant = Participant.builder()
                .id(100)
                .name("Lakers")
                .type("team")
                .build();

        assertEquals(100, participant.getId());
        assertEquals("Lakers", participant.getName());
        assertEquals("team", participant.getType());
    }

    @Test
    void testSettersAndGetters() {
        Participant participant = new Participant();
        participant.setParticipantId(1);
        participant.setId(100);
        participant.setType("team");
        participant.setName("Lakers");
        participant.setMarketId(5L);

        assertEquals(1, participant.getParticipantId());
        assertEquals(100, participant.getId());
        assertEquals("team", participant.getType());
        assertEquals("Lakers", participant.getName());
        assertEquals(5L, participant.getMarketId());
    }
}
