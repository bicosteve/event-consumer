package com.bix.event_consumer.cache;

import com.bix.event_consumer.models.Event;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventCacheReconcilerTest {
    private final EventReadModelRepository repository = mock(EventReadModelRepository.class);
    private final RedisEventCacheWriter writer = mock(RedisEventCacheWriter.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final OffsetDateTime now = OffsetDateTime.parse("2030-01-01T00:00:00Z");
    private final EventReadModelRepository.UpcomingCursor watermark =
            new EventReadModelRepository.UpcomingCursor(now.plusDays(2), "z");

    @Test
    void renewsBeforeEachBatchAndReleasesOwnedLock() {
        acquired();
        when(repository.findUpcomingWatermark(now)).thenReturn(Optional.of(watermark));
        Event event = Event.builder().eventId("a").eventDate(now.plusDays(1)).build();
        when(repository.findUpcoming(now, null, watermark, 2)).thenReturn(List.of(event));
        scriptResults(1L, 1L);

        reconciler().reconcile();

        verify(writer).removeExpired();
        verify(writer).refresh(event);
        verify(redis).execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString(), eq("240000"));
        verify(redis).execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString());
    }

    @Test
    void abortsBatchReadWhenLeaseRenewalIsLostAndStillUsesTokenSafeRelease() {
        acquired();
        when(repository.findUpcomingWatermark(now)).thenReturn(Optional.of(watermark));
        scriptResults(0L, 1L);

        reconciler().reconcile();

        verify(repository, never()).findUpcoming(any(), any(), any(), anyInt());
        verify(redis).execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString(), eq("240000"));
        verify(redis).execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString());
    }

    @Test
    void abortsRemainingEventWritesWhenRenewalIsLostMidBatch() {
        acquired();
        when(repository.findUpcomingWatermark(now)).thenReturn(Optional.of(watermark));
        Event first = Event.builder().eventId("a").eventDate(now.plusHours(1)).build();
        Event second = Event.builder().eventId("b").eventDate(now.plusHours(2)).build();
        when(repository.findUpcoming(now, null, watermark, 3)).thenReturn(List.of(first, second));
        scriptResults(1L, 0L, 1L);
        AtomicLong monotonicMillis = new AtomicLong(0);
        doAnswer(ignored -> { monotonicMillis.set(Duration.ofMinutes(2).toMillis()); return null; }).when(writer).refresh(first);

        new EventCacheReconciler(repository, writer, redis, Clock.fixed(now.toInstant(), ZoneOffset.UTC),
                3, Duration.ofMinutes(4), Duration.ofMinutes(5), monotonicMillis::get).reconcile();

        verify(writer).refresh(first);
        verify(writer, never()).refresh(second);
        verify(redis, times(2)).execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString(), eq("240000"));
    }

 @Test
 void heartbeatRenewsDuringBlockingWriterAndLeaseLossPreventsSubsequentWrites() throws Exception {
 when(redis.opsForValue()).thenReturn(values);
 when(values.setIfAbsent(eq(EventCacheReconciler.LOCK_KEY), anyString(), any(Duration.class))).thenReturn(true);
 when(repository.findUpcomingWatermark(now)).thenReturn(Optional.of(watermark));
 Event first = Event.builder().eventId("a").eventDate(now.plusHours(1)).build();
 Event second = Event.builder().eventId("b").eventDate(now.plusHours(2)).build();
 when(repository.findUpcoming(now, null, watermark, 2)).thenReturn(List.of(first, second));
 CountDownLatch writerStarted = new CountDownLatch(1);
 CountDownLatch unblockWriter = new CountDownLatch(1);
 when(redis.execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString(), eq("90")))
 .thenAnswer(ignored -> writerStarted.getCount() == 0 ? 0L : 1L);
 doAnswer(ignored -> {
   writerStarted.countDown();
   assertTrue(unblockWriter.await(5, TimeUnit.SECONDS));
   return null;
  }).when(writer).refresh(first);

  Thread reconciliation = Thread.ofVirtual().start(() -> reconciler(Duration.ofMillis(90)).reconcile());
  assertTrue(writerStarted.await(5, TimeUnit.SECONDS));
  verify(redis, timeout(5_000).atLeast(2)).execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString(), eq("90"));
  unblockWriter.countDown();
  reconciliation.join(5_000);

  verify(writer).refresh(first);
  verify(writer, never()).refresh(second);
 }

 @Test
 void cleanupFailureDoesNotPreventPerEventProcessingAndPerEventFailureContinues() {
        acquired();
        doThrow(new IllegalStateException("cleanup")).when(writer).removeExpired();
        when(repository.findUpcomingWatermark(now)).thenReturn(Optional.of(watermark));
        Event first = Event.builder().eventId("a").eventDate(now.plusHours(1)).build();
        Event second = Event.builder().eventId("b").eventDate(now.plusHours(2)).build();
        when(repository.findUpcoming(now, null, watermark, 3)).thenReturn(List.of(first, second));
        doThrow(new IllegalStateException("one event")).when(writer).refresh(first);
        scriptResults(1L, 1L);

        new EventCacheReconciler(repository, writer, redis, Clock.fixed(now.toInstant(), ZoneOffset.UTC),
                3, Duration.ofMinutes(4), Duration.ofMinutes(5)).reconcile();

        verify(writer).refresh(second);
    }

    @Test
    void validatesPositiveConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new EventCacheReconciler(repository, writer, redis,
                Clock.systemUTC(), 0, Duration.ofMinutes(1), Duration.ofMinutes(1)));
        assertThrows(IllegalArgumentException.class, () -> new EventCacheReconciler(repository, writer, redis,
                Clock.systemUTC(), 1, Duration.ZERO, Duration.ofMinutes(1)));
        assertThrows(IllegalArgumentException.class, () -> new EventCacheReconciler(repository, writer, redis,
                Clock.systemUTC(), 1, Duration.ofMinutes(1), Duration.ZERO));
    }

 private EventCacheReconciler reconciler() {
  return reconciler(Duration.ofMinutes(4));
 }

 private EventCacheReconciler reconciler(Duration lockTtl) {
  return new EventCacheReconciler(repository, writer, redis, Clock.fixed(now.toInstant(), ZoneOffset.UTC),
   2, lockTtl, Duration.ofMinutes(5));
 }


    private void acquired() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(eq(EventCacheReconciler.LOCK_KEY), anyString(), eq(Duration.ofMinutes(4))))
                .thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    private void scriptResults(Long renewal, Long release) {
        scriptResults(new Long[]{renewal}, release);
    }

    @SuppressWarnings("unchecked")
    private void scriptResults(Long renewal, Long nextRenewal, Long release) {
        scriptResults(new Long[]{renewal, nextRenewal}, release);
    }

    @SuppressWarnings("unchecked")
    private void scriptResults(Long[] renewals, Long release) {
        when(redis.execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString(), eq("240000")))
                .thenReturn(renewals[0], java.util.Arrays.copyOfRange(renewals, 1, renewals.length));
        when(redis.execute(any(RedisScript.class), eq(List.of(EventCacheReconciler.LOCK_KEY)), anyString()))
                .thenReturn(release);
    }
}
