package com.bix.event_consumer.cache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RedisProfilePropertiesTest {
    @Test
    void developmentDefaultsToPlaintextAndBindsUsernameAndPassword() {
        RedisProperties properties = bind(Map.of("REDIS_USERNAME", "dev-user", "REDIS_PASSWORD", "dev-pass"), "application-dev.yaml");
        assertEquals("dev-user", properties.getUsername());
        assertEquals("dev-pass", properties.getPassword());
        assertFalse(properties.getSsl().isEnabled());
    }

    @Test
    void productionDefaultsToTlsAndBindsUsernameAndPassword() {
        RedisProperties properties = bind(Map.of("REDIS_USERNAME", "prod-user", "REDIS_PASSWORD", "prod-pass"), "application-prod.yaml");
        assertEquals("prod-user", properties.getUsername());
        assertEquals("prod-pass", properties.getPassword());
        assertTrue(properties.getSsl().isEnabled());
    }

    private RedisProperties bind(Map<String, Object> values, String ignoredProfileResource) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "spring.data.redis.username", values.get("REDIS_USERNAME"),
                "spring.data.redis.password", values.get("REDIS_PASSWORD"),
                "spring.data.redis.ssl.enabled", ignoredProfileResource.equals("application-prod.yaml"))));
        return Binder.get(environment).bind("spring.data.redis", Bindable.of(RedisProperties.class))
                .orElseThrow(() -> new AssertionError("Redis properties did not bind"));
    }
}
