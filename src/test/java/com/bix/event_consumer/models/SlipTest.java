package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SlipTest {

    @Test
    void testNoArgsConstructor() {
        Slip slip = new Slip();
        assertNotNull(slip);
        assertNull(slip.getBetSlipId());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Slip slip = new Slip(1L, 2L, "evt-1", 1, 100, 5, "Moneyline",
                "Team A", BigDecimal.valueOf(2.5), "hcp=1.5", 1, now, now);

        assertEquals(1L, slip.getBetSlipId());
        assertEquals(2L, slip.getBetId());
        assertEquals("evt-1", slip.getEventId());
        assertEquals(1, slip.getSportId());
        assertEquals(100, slip.getTeamId());
        assertEquals(5, slip.getMarketId());
        assertEquals("Moneyline", slip.getMarketName());
        assertEquals("Team A", slip.getParticipantName());
        assertEquals(BigDecimal.valueOf(2.5), slip.getOdds());
        assertEquals("hcp=1.5", slip.getSpecialBetValue());
        assertEquals(1, slip.getStatus());
        assertEquals(now, slip.getCreatedAt());
    }

    @Test
    void testBuilder() {
        Slip slip = Slip.builder()
                .betSlipId(1L)
                .betId(2L)
                .eventId("evt-1")
                .marketName("Moneyline")
                .build();

        assertEquals(1L, slip.getBetSlipId());
        assertEquals(2L, slip.getBetId());
        assertEquals("evt-1", slip.getEventId());
        assertEquals("Moneyline", slip.getMarketName());
    }

    @Test
    void testSettersAndGetters() {
        Slip slip = new Slip();
        slip.setBetSlipId(1L);
        slip.setBetId(2L);
        slip.setEventId("evt-1");
        slip.setSportId(1);
        slip.setTeamId(100);
        slip.setMarketId(5);
        slip.setMarketName("Moneyline");
        slip.setParticipantName("Team A");
        slip.setOdds(BigDecimal.valueOf(2.5));
        slip.setSpecialBetValue("hcp=1.5");
        slip.setStatus(1);

        assertEquals(1L, slip.getBetSlipId());
        assertEquals(2L, slip.getBetId());
        assertEquals("evt-1", slip.getEventId());
        assertEquals(1, slip.getSportId());
        assertEquals(100, slip.getTeamId());
        assertEquals(5, slip.getMarketId());
        assertEquals("Moneyline", slip.getMarketName());
        assertEquals("Team A", slip.getParticipantName());
        assertEquals(BigDecimal.valueOf(2.5), slip.getOdds());
        assertEquals("hcp=1.5", slip.getSpecialBetValue());
        assertEquals(1, slip.getStatus());
    }
}
