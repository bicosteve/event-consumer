package com.bix.event_consumer.cache;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Schedule;
import com.bix.event_consumer.models.Score;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class RedisEventCacheWriterTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T00:00:00.123Z"), ZoneOffset.UTC);
    private final RedisEventCacheWriter writer = new RedisEventCacheWriter(
            redis, new ObjectMapper().findAndRegisterModules(), clock);

    @Test
    void luaAtomicallyAppliesExpiryBoundaryUsingTheCapturedNow() {
        writer.refresh(event("2030-01-02T00:00:00.456Z"));

        ArgumentCaptor<RedisScript<Long>> script = redisScriptCaptor();
        verify(redis).execute(script.capture(),
                eq(List.of("event-cache:v1:event:event-1", "event-cache:v1:upcoming")),
                any(String.class), eq("event-1"), eq("1893456000123"), eq("1893542400456"));
        assertTrue(script.getValue().getScriptAsString().contains("tonumber(ARGV[3]) >= tonumber(ARGV[4])"));
        assertTrue(script.getValue().getScriptAsString().contains("DEL"));
        assertTrue(script.getValue().getScriptAsString().contains("ZREM"));
        assertTrue(script.getValue().getScriptAsString().contains("PXAT"));
        assertTrue(script.getValue().getScriptAsString().contains("ZADD"));
        verifyNoMoreInteractions(redis);
    }

    @Test
    void expiryAtCapturedNowIsPassedToLuaForAtomicDeletion() {
        writer.refresh(event("2030-01-01T00:00:00.123Z"));

        ArgumentCaptor<RedisScript<Long>> script = redisScriptCaptor();
        verify(redis).execute(script.capture(),
                eq(List.of("event-cache:v1:event:event-1", "event-cache:v1:upcoming")),
                any(String.class), eq("event-1"), eq("1893456000123"), eq("1893456000123"));
        assertEquals(Long.class, script.getValue().getResultType());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ArgumentCaptor<RedisScript<Long>> redisScriptCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(RedisScript.class);
    }

    private Event event(String date) {
        return Event.builder()
                .eventId("event-1")
                .eventUuid("uuid")
                .sportId(1)
                .eventDate(OffsetDateTime.parse(date))
                .schedule(Schedule.builder().build())
                .score(Score.builder().eventStatus(EventStatus.STATUS_SCHEDULED).build())
                .build();
    }
}
