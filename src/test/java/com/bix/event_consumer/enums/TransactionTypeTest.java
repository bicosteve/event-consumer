package com.bix.event_consumer.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTypeTest {

    @Test
    void testEnumConstantsExist() {
        assertNotNull(TransactionType.valueOf("CREDIT"));
        assertNotNull(TransactionType.valueOf("DEBIT"));
        assertNotNull(TransactionType.valueOf("REFUND"));
    }

    @Test
    void testStatusCodes() {
        assertEquals(1, TransactionType.CREDIT.getStatus());
        assertEquals(2, TransactionType.DEBIT.getStatus());
        assertEquals(3, TransactionType.REFUND.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREDIT", "credit", "Credit"})
    void testFromValueWithCredit(String value) {
        assertEquals(TransactionType.CREDIT, TransactionType.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEBIT", "debit", "Debit"})
    void testFromValueWithDebit(String value) {
        assertEquals(TransactionType.DEBIT, TransactionType.fromValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"REFUND", "refund", "Refund"})
    void testFromValueWithRefund(String value) {
        assertEquals(TransactionType.REFUND, TransactionType.fromValue(value));
    }

    @Test
    void testFromValueWithUnknownThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionType.fromValue("UNKNOWN")
        );
        assertEquals("Unknown transaction type UNKNOWN ", exception.getMessage());
    }

    @Test
    void testFromValueWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.fromValue(null));
    }

    @Test
    void testFromValueWithEmptyStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.fromValue(""));
    }

    @Test
    void testValuesReturnsAllConstants() {
        assertEquals(3, TransactionType.values().length);
    }
}
