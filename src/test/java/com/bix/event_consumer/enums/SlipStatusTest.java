package com.bix.event_consumer.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlipStatusTest {

    @Test
    void testEnumConstantsExist() {
        assertNotNull(SlipStatus.valueOf("PENDING"));
        assertNotNull(SlipStatus.valueOf("LOST"));
        assertNotNull(SlipStatus.valueOf("WON"));
        assertNotNull(SlipStatus.valueOf("VOID"));
    }

    @Test
    void testStatusCodes() {
        assertEquals(1, SlipStatus.PENDING.getStatus());
        assertEquals(3, SlipStatus.LOST.getStatus());
        assertEquals(5, SlipStatus.WON.getStatus());
        assertEquals(7, SlipStatus.VOID.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "pending", "Pending"})
    void testFromValueWithPending(String value) {
        assertEquals(SlipStatus.PENDING, SlipStatus.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOST", "lost", "Lost"})
    void testFromValueWithLost(String value) {
        assertEquals(SlipStatus.LOST, SlipStatus.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WON", "won", "Won"})
    void testFromValueWithWon(String value) {
        assertEquals(SlipStatus.WON, SlipStatus.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"VOID", "void", "Void"})
    void testFromValueWithVoid(String value) {
        assertEquals(SlipStatus.VOID, SlipStatus.fromValue(value));
    }

    @Test
    void testFromValueWithUnknownThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SlipStatus.fromValue("UNKNOWN")
        );
        assertEquals("Unknown slip status UNKNOWN ", exception.getMessage());
    }

    @Test
    void testFromValueWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> SlipStatus.fromValue(null));
    }

    @Test
    void testFromValueWithEmptyStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> SlipStatus.fromValue(""));
    }

    @Test
    void testValuesReturnsAllConstants() {
        assertEquals(4, SlipStatus.values().length);
    }
}
