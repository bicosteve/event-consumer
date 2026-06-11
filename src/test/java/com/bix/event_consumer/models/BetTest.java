package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetTest {

    @Test
    void testNoArgsConstructor() {
        Bet bet = new Bet();
        assertNotNull(bet);
        assertNull(bet.getBetId());
        assertNull(bet.getSlips());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Slip> slips = new ArrayList<>();
        Bet bet = new Bet(1L, 2L, BigDecimal.TEN, 0, 1, BigDecimal.valueOf(2.5),
                BigDecimal.valueOf(25), now, now, slips);

        assertEquals(1L, bet.getBetId());
        assertEquals(2L, bet.getProfileId());
        assertEquals(BigDecimal.TEN, bet.getStake());
        assertEquals(0, bet.getIsBonus());
        assertEquals(1, bet.getStatus());
        assertEquals(BigDecimal.valueOf(2.5), bet.getTotalOdds());
        assertEquals(BigDecimal.valueOf(25), bet.getPossibleWin());
        assertEquals(slips, bet.getSlips());
    }

    @Test
    void testBuilder() {
        Bet bet = Bet.builder()
                .betId(1L)
                .profileId(2L)
                .stake(BigDecimal.TEN)
                .isBonus(0)
                .status(1)
                .totalOdds(BigDecimal.valueOf(2.5))
                .possibleWin(BigDecimal.valueOf(25))
                .build();

        assertEquals(1L, bet.getBetId());
        assertEquals(2L, bet.getProfileId());
        assertEquals(BigDecimal.TEN, bet.getStake());
    }

    @Test
    void testSettersAndGetters() {
        Bet bet = new Bet();
        bet.setBetId(10L);
        bet.setProfileId(20L);
        bet.setStake(BigDecimal.TEN);
        bet.setIsBonus(1);
        bet.setStatus(3);
        bet.setTotalOdds(BigDecimal.valueOf(3.0));
        bet.setPossibleWin(BigDecimal.valueOf(30));

        assertEquals(10L, bet.getBetId());
        assertEquals(20L, bet.getProfileId());
        assertEquals(BigDecimal.TEN, bet.getStake());
        assertEquals(1, bet.getIsBonus());
        assertEquals(3, bet.getStatus());
        assertEquals(BigDecimal.valueOf(3.0), bet.getTotalOdds());
        assertEquals(BigDecimal.valueOf(30), bet.getPossibleWin());
    }

    @Test
    void testSlipsList() {
        Bet bet = new Bet();
        List<Slip> slips = new ArrayList<>();
        Slip slip = Slip.builder().betSlipId(1L).build();
        slips.add(slip);

        bet.setSlips(slips);
        assertEquals(1, bet.getSlips().size());
        assertTrue(bet.getSlips().contains(slip));
    }
}
