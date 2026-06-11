package com.bix.event_consumer.models;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LineTest {

    @Test
    void testNoArgsConstructor() {
        Line line = new Line();
        assertNotNull(line);
        assertNull(line.getId());
    }

    @Test
    void testAllArgsConstructor() {
        Map<String, Price> prices = new HashMap<>();
        Line line = new Line("line-1", "hcp=2.5", prices);

        assertEquals("line-1", line.getId());
        assertEquals("hcp=2.5", line.getValue());
        assertEquals(prices, line.getPrices());
    }

    @Test
    void testBuilder() {
        Line line = Line.builder()
                .id("line-1")
                .value("hcp=2.5")
                .build();

        assertEquals("line-1", line.getId());
        assertEquals("hcp=2.5", line.getValue());
    }

    @Test
    void testSettersAndGetters() {
        Line line = new Line();
        line.setId("line-1");
        line.setValue("hcp=2.5");

        Map<String, Price> prices = new HashMap<>();
        line.setPrices(prices);

        assertEquals("line-1", line.getId());
        assertEquals("hcp=2.5", line.getValue());
        assertEquals(prices, line.getPrices());
    }
}
