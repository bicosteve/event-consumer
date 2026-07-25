package com.bix.event_consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainTests {

 @Test
 void mainIsSpringBootApplication() {
 assertNotNull(Main.class.getAnnotation(SpringBootApplication.class));
 }

}
