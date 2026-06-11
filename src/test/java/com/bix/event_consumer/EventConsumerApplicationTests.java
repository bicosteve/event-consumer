package com.bix.event_consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context-load test that boots the full Spring application.
 *
 * <p>This test requires a live MySQL instance, RabbitMQ broker, and the full
 * configuration profile to be present (e.g. via a populated {@code .env} file).
 * It is therefore disabled by default and is meant to be run as an
 * <strong>integration test</strong> in a dedicated environment, not as part of
 * the unit-test pipeline.</p>
 */
@SpringBootTest
@Disabled("Integration test - requires MySQL, RabbitMQ and full .env; run separately from the unit-test suite")
class EventConsumerApplicationTests {

	@Test
	void contextLoads() {
	}

}
