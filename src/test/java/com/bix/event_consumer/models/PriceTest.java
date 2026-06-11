package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PriceTest {

    @Test
    void testNoArgsConstructor() {
        Price price = new Price();
        assertNotNull(price);
    }

    @Test
    void testAllArgsConstructor() {
        Price price = new Price(
                1, "p1", 100, 0, true,
                1L, 23, "hcp=2.5", "line-1",
                null, null, null
        );
        assertEquals(1, price.getPriceId());
        assertEquals("p1", price.getId());
        assertEquals(100, price.getPrice());
        assertEquals(0, price.getPriceDelta());
        assertEquals(true, price.isMainLine());
        assertEquals(1L, price.getParticipantId());
        assertEquals(23, price.getBookMarkerId());
        assertEquals("hcp=2.5", price.getHandicapValue());
        assertEquals("line-1", price.getLineId());
    }

    @Test
    void testBuilder() {
        Price price = Price.builder()
                .id("p1")
                .price(100)
                .build();

        assertEquals("p1", price.getId());
        assertEquals(100, price.getPrice());
    }

    @Test
    void testSettersAndGetters() {
        Price price = new Price();
        price.setId("p1");
        price.setPrice(-150);
        price.setPriceDelta(10);
        price.setMainLine(false);
        price.setPriceId(5);
        price.setParticipantId(2L);
        price.setBookMarkerId(23);
        price.setHandicapValue("hcp=3.5");
        price.setLineId("line-1");

        assertEquals("p1", price.getId());
        assertEquals(-150, price.getPrice());
        assertEquals(10, price.getPriceDelta());
        assertEquals(false, price.isMainLine());
        assertEquals(5, price.getPriceId());
        assertEquals(2L, price.getParticipantId());
        assertEquals(23, price.getBookMarkerId());
        assertEquals("hcp=3.5", price.getHandicapValue());
        assertEquals("line-1", price.getLineId());
    }

    @Test
    void testGetOddsWithPositivePrice() {
        // +100 American odds = 2.0 decimal odds
        Price price = Price.builder().price(100).build();
        BigDecimal odds = price.getOdds();
        assertEquals(0, new BigDecimal("2.00").compareTo(odds));
    }

    @Test
    void testGetOddsWithPositivePrice150() {
        // +150 American odds = 2.5 decimal odds
        Price price = Price.builder().price(150).build();
        BigDecimal odds = price.getOdds();
        assertEquals(0, new BigDecimal("2.50").compareTo(odds));
    }

    @Test
    void testGetOddsWithNegativePrice() {
        // -100 American odds = 2.0 decimal odds
        Price price = Price.builder().price(-100).build();
        BigDecimal odds = price.getOdds();
        assertEquals(0, new BigDecimal("2.00").compareTo(odds));
    }

    @Test
    void testGetOddsWithNegativePrice200() {
        // -200 American odds = 1.5 decimal odds
        Price price = Price.builder().price(-200).build();
        BigDecimal odds = price.getOdds();
        assertEquals(0, new BigDecimal("1.50").compareTo(odds));
    }

    @Test
    void testGetOddsWithZeroPrice() {
        // 0 should be treated as positive (>= 0)
        Price price = Price.builder().price(0).build();
        BigDecimal odds = price.getOdds();
        assertEquals(0, new BigDecimal("1.00").compareTo(odds));
    }

    @ParameterizedTest
    @CsvSource({
            "100, 2.00",
            "150, 2.50",
            "200, 3.00",
            "300, 4.00",
            "-100, 2.00",
            "-150, 1.67",
            "-200, 1.50",
            "-300, 1.33"
    })
    void testGetOddsForVariousAmericanOdds(int price, String expectedDecimal) {
        Price p = Price.builder().price(price).build();
        BigDecimal odds = p.getOdds();
        assertEquals(0, new BigDecimal(expectedDecimal).compareTo(odds),
                "Price " + price + " should produce decimal odds " + expectedDecimal
                        + " but got " + odds);
    }
}
