package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MarketTest {

    @Test
    void testNoArgsConstructor() {
        Market market = new Market();
        assertNotNull(market);
        assertNull(market.getId());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Participant> participants = new ArrayList<>();
        Market market = new Market(1L, 5, 0, "Moneyline", "Match winner",
                now, now, "evt-1", participants);

        assertEquals(1L, market.getId());
        assertEquals(5, market.getMarketId());
        assertEquals(0, market.getPeriodId());
        assertEquals("Moneyline", market.getName());
        assertEquals("Match winner", market.getMarketDescription());
        assertEquals("evt-1", market.getEventId());
        assertEquals(participants, market.getParticipants());
    }

    @Test
    void testBuilder() {
        Market market = Market.builder()
                .marketId(5)
                .name("Moneyline")
                .marketDescription("Match winner")
                .build();

        assertEquals(5, market.getMarketId());
        assertEquals("Moneyline", market.getName());
        assertEquals("Match winner", market.getMarketDescription());
    }

    @Test
    void testSettersAndGetters() {
        Market market = new Market();
        market.setId(1L);
        market.setMarketId(5);
        market.setPeriodId(1);
        market.setName("Totals");
        market.setMarketDescription("Over/Under");
        market.setEventId("evt-1");

        assertEquals(1L, market.getId());
        assertEquals(5, market.getMarketId());
        assertEquals(1, market.getPeriodId());
        assertEquals("Totals", market.getName());
        assertEquals("Over/Under", market.getMarketDescription());
        assertEquals("evt-1", market.getEventId());
    }
}
