package com.bix.event_consumer.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BetStatusTest {

    @Test
    void testEnumConstantsExist() {
        assertNotNull(BetStatus.valueOf("PENDING"));
        assertNotNull(BetStatus.valueOf("LOST"));
        assertNotNull(BetStatus.valueOf("WON"));
        assertNotNull(BetStatus.valueOf("VOID"));
    }

    @Test
    void testStatusCodes() {
        assertEquals(1, BetStatus.PENDING.getStatus());
        assertEquals(3, BetStatus.LOST.getStatus());
        assertEquals(5, BetStatus.WON.getStatus());
        assertEquals(7, BetStatus.VOID.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "pending", "Pending", "PeNdInG"})
    void testFromValueWithPending(String value) {
        assertEquals(BetStatus.PENDING, BetStatus.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOST", "lost", "Lost", "LoSt"})
    void testFromValueWithLost(String value) {
        assertEquals(BetStatus.LOST, BetStatus.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WON", "won", "Won", "WoN"})
    void testFromValueWithWon(String value) {
        assertEquals(BetStatus.WON, BetStatus.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"VOID", "void", "Void", "VoId"})
    void testFromValueWithVoid(String value) {
        assertEquals(BetStatus.VOID, BetStatus.fromValue(value));
    }

    @Test
    void testFromValueWithUnknownThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BetStatus.fromValue("UNKNOWN")
        );
        assertEquals("Unknown bet status UNKNOWN ", exception.getMessage());
    }

    @Test
    void testFromValueWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> BetStatus.fromValue(null));
    }

    @Test
    void testFromValueWithEmptyStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> BetStatus.fromValue(""));
    }

    @Test
    void testValuesReturnsAllConstants() {
        assertEquals(4, BetStatus.values().length);
    }
}
