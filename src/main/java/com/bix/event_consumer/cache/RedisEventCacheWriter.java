package com.bix.event_consumer.cache;

import com.bix.event_consumer.models.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class RedisEventCacheWriter {
    public static final String KEY_PREFIX = "event-cache:v1:event:";
    public static final String UPCOMING_KEY = "event-cache:v1:upcoming";

    private static final DefaultRedisScript<Long> WRITE_WITH_EXPIRY_BOUNDARY = new DefaultRedisScript<>(
            "if tonumber(ARGV[3]) >= tonumber(ARGV[4]) then "
                    + "redis.call('DEL', KEYS[1]); return redis.call('ZREM', KEYS[2], ARGV[2]); end; "
                    + "redis.call('SET', KEYS[1], ARGV[1], 'PXAT', ARGV[4]); "
                    + "return redis.call('ZADD', KEYS[2], ARGV[4], ARGV[2])", Long.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RedisEventCacheWriter(StringRedisTemplate redis, ObjectMapper objectMapper, Clock clock) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public void refresh(Event event) {
        long capturedNowMillis = clock.millis();
        long eventEpochMillis = event.getEventDate().toInstant().toEpochMilli();
        String eventId = event.getEventId();
        List<String> keys = List.of(KEY_PREFIX + eventId, UPCOMING_KEY);
        try {
            redis.execute(
                    WRITE_WITH_EXPIRY_BOUNDARY,
                    keys,
                    objectMapper.writeValueAsString(EventCacheProjection.from(event)),
                    eventId,
                    String.valueOf(capturedNowMillis),
                    String.valueOf(eventEpochMillis));

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize event " + eventId, e);
        }
    }

    public void removeExpired() {
        redis.opsForZSet().removeRangeByScore(UPCOMING_KEY, Double.NEGATIVE_INFINITY, clock.millis());
    }
}
