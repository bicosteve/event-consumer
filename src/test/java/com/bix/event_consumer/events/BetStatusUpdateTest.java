package com.bix.event_consumer.events;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BetStatusUpdateTest {

    @Test
    void testNoArgsConstructor() {
        BetStatusUpdate update = new BetStatusUpdate();
        assertNotNull(update);
        assertNull(update.getBetId());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        BetStatusUpdate update = new BetStatusUpdate(1L, 2L, BigDecimal.TEN,
                1, 5, BigDecimal.valueOf(50), 1, "ref-1", now);

        assertEquals(1L, update.getBetId());
        assertEquals(2L, update.getProfileId());
        assertEquals(BigDecimal.TEN, update.getAmount());
        assertEquals(1, update.getPreviousStatus());
        assertEquals(5, update.getCurrentStatus());
        assertEquals(BigDecimal.valueOf(50), update.getPossibleWin());
        assertEquals(1, update.getType());
        assertEquals("ref-1", update.getReference());
        assertEquals(now, update.getUpdateAt());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        BetStatusUpdate update = BetStatusUpdate.builder()
                .betId(1L)
                .profileId(2L)
                .amount(BigDecimal.TEN)
                .previousStatus(1)
                .currentStatus(5)
                .possibleWin(BigDecimal.valueOf(50))
                .type(1)
                .reference("ref-1")
                .updateAt(now)
                .build();

        assertEquals(1L, update.getBetId());
        assertEquals(2L, update.getProfileId());
        assertEquals(BigDecimal.TEN, update.getAmount());
        assertEquals(1, update.getPreviousStatus());
        assertEquals(5, update.getCurrentStatus());
        assertEquals(BigDecimal.valueOf(50), update.getPossibleWin());
        assertEquals(1, update.getType());
        assertEquals("ref-1", update.getReference());
        assertEquals(now, update.getUpdateAt());
    }

    @Test
    void testSettersAndGetters() {
        BetStatusUpdate update = new BetStatusUpdate();
        update.setBetId(10L);
        update.setProfileId(20L);
        update.setAmount(BigDecimal.TEN);
        update.setPreviousStatus(1);
        update.setCurrentStatus(5);
        update.setPossibleWin(BigDecimal.valueOf(50));
        update.setType(1);
        update.setReference("ref-1");

        assertEquals(10L, update.getBetId());
        assertEquals(20L, update.getProfileId());
        assertEquals(BigDecimal.TEN, update.getAmount());
        assertEquals(1, update.getPreviousStatus());
        assertEquals(5, update.getCurrentStatus());
        assertEquals(BigDecimal.valueOf(50), update.getPossibleWin());
        assertEquals(1, update.getType());
        assertEquals("ref-1", update.getReference());
    }
}
