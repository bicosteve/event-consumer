package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConferenceTest {

    @Test
    void testNoArgsConstructor() {
        Conference conference = new Conference();
        assertNotNull(conference);
        assertNull(conference.getConferenceId());
    }

    @Test
    void testAllArgsConstructor() {
        Conference conference = new Conference(1, 2, "Western");

        assertEquals(1, conference.getConferenceId());
        assertEquals(2, conference.getSportId());
        assertEquals("Western", conference.getName());
    }

    @Test
    void testBuilder() {
        Conference conference = Conference.builder()
                .conferenceId(1)
                .sportId(2)
                .name("Western")
                .build();

        assertEquals(1, conference.getConferenceId());
        assertEquals(2, conference.getSportId());
        assertEquals("Western", conference.getName());
    }

    @Test
    void testSettersAndGetters() {
        Conference conference = new Conference();
        conference.setConferenceId(1);
        conference.setSportId(2);
        conference.setName("Western");

        assertEquals(1, conference.getConferenceId());
        assertEquals(2, conference.getSportId());
        assertEquals("Western", conference.getName());
    }
}
