package com.bix.event_consumer.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventStatusTest {

    @Test
    void testEnumConstantsExist() {
        assertNotNull(EventStatus.valueOf("STATUS_SCHEDULED"));
        assertNotNull(EventStatus.valueOf("STATUS_IN_PROGRESS"));
        assertNotNull(EventStatus.valueOf("STATUS_FINAL"));
        assertNotNull(EventStatus.valueOf("STATUS_POSTPONED"));
        assertNotNull(EventStatus.valueOf("STATUS_CANCELED"));
        assertNotNull(EventStatus.valueOf("STATUS_SUSPENDED"));
        assertNotNull(EventStatus.valueOf("STATUS_DELAYED"));
        assertNotNull(EventStatus.valueOf("STATUS_RAIN_DELAY"));
        assertNotNull(EventStatus.valueOf("STATUS_HALFTIME"));
        assertNotNull(EventStatus.valueOf("STATUS_END_PERIOD"));
        assertNotNull(EventStatus.valueOf("STATUS_END_OF_REGULATION"));
        assertNotNull(EventStatus.valueOf("STATUS_OVERTIME"));
        assertNotNull(EventStatus.valueOf("STATUS_FIRST_HALF"));
        assertNotNull(EventStatus.valueOf("STATUS_SECOND_HALF"));
        assertNotNull(EventStatus.valueOf("STATUS_FULL_TIME"));
        assertNotNull(EventStatus.valueOf("STATUS_FINAL_AET"));
        assertNotNull(EventStatus.valueOf("STATUS_FINAL_PEN"));
        assertNotNull(EventStatus.valueOf("STATUS_SHOOTOUT"));
        assertNotNull(EventStatus.valueOf("STATUS_FORFEIT"));
    }

    @Test
    void testStatusCodes() {
        assertEquals(0, EventStatus.STATUS_SCHEDULED.getCode());
        assertEquals(1, EventStatus.STATUS_IN_PROGRESS.getCode());
        assertEquals(2, EventStatus.STATUS_FINAL.getCode());
        assertEquals(3, EventStatus.STATUS_POSTPONED.getCode());
        assertEquals(4, EventStatus.STATUS_CANCELED.getCode());
        assertEquals(5, EventStatus.STATUS_SUSPENDED.getCode());
        assertEquals(6, EventStatus.STATUS_DELAYED.getCode());
        assertEquals(7, EventStatus.STATUS_RAIN_DELAY.getCode());
        assertEquals(8, EventStatus.STATUS_HALFTIME.getCode());
        assertEquals(9, EventStatus.STATUS_END_PERIOD.getCode());
        assertEquals(10, EventStatus.STATUS_END_OF_REGULATION.getCode());
        assertEquals(11, EventStatus.STATUS_OVERTIME.getCode());
        assertEquals(12, EventStatus.STATUS_FIRST_HALF.getCode());
        assertEquals(13, EventStatus.STATUS_SECOND_HALF.getCode());
        assertEquals(14, EventStatus.STATUS_FULL_TIME.getCode());
        assertEquals(15, EventStatus.STATUS_FINAL_AET.getCode());
        assertEquals(16, EventStatus.STATUS_FINAL_PEN.getCode());
        assertEquals(17, EventStatus.STATUS_SHOOTOUT.getCode());
        assertEquals(18, EventStatus.STATUS_FORFEIT.getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"STATUS_FINAL", "status_final", "Status_Final"})
    void testFromValueWithStatusFinal(String value) {
        assertEquals(EventStatus.STATUS_FINAL, EventStatus.fromValue(value));
    }

    @Test
    void testFromValueWithUnknownThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EventStatus.fromValue("UNKNOWN_STATUS")
        );
        assertEquals("Unknown event status UNKNOWN_STATUS ", exception.getMessage());
    }

    @Test
    void testFromValueWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> EventStatus.fromValue(null));
    }

    @Test
    void testFromValueWithEmptyStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> EventStatus.fromValue(""));
    }

    @ParameterizedTest
    @CsvSource({
            "0, STATUS_SCHEDULED",
            "1, STATUS_IN_PROGRESS",
            "2, STATUS_FINAL",
            "3, STATUS_POSTPONED",
            "4, STATUS_CANCELED",
            "5, STATUS_SUSPENDED",
            "14, STATUS_FULL_TIME",
            "15, STATUS_FINAL_AET",
            "16, STATUS_FINAL_PEN",
            "18, STATUS_FORFEIT"
    })
    void testFromCodeReturnsExpectedStatus(int code, String expectedName) {
        assertEquals(EventStatus.valueOf(expectedName), EventStatus.fromCode(code));
    }

    @Test
    void testFromCodeWithUnknownCodeReturnsDefault() {
        // When an unknown code is provided, the default STATUS_SCHEDULED is returned
        assertEquals(EventStatus.STATUS_SCHEDULED, EventStatus.fromCode(9999));
        assertEquals(EventStatus.STATUS_SCHEDULED, EventStatus.fromCode(-1));
    }

    @Test
    void testValuesReturnsAllConstants() {
        assertEquals(19, EventStatus.values().length);
    }
}
