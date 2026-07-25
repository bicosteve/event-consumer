package com.bix.event_consumer.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

@Component
@Slf4j
public class EventCacheReconciler {
   static final String LOCK_KEY = "event-cache:v1:reconcile:lock";
   private static final DefaultRedisScript<Long> RENEW = new DefaultRedisScript<>(
   "if redis.call('GET', KEYS[1]) == ARGV[1] then "
   + "return redis.call('PEXPIRE', KEYS[1], ARGV[2]) else return 0 end", Long.class);

   private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
   "if redis.call('GET', KEYS[1]) == ARGV[1] then "
   + "return redis.call('DEL', KEYS[1]) else return 0 end", Long.class);

   private final EventReadModelRepository repository;
   private final RedisEventCacheWriter writer;
   private final StringRedisTemplate redis;
   private final Clock clock;
   private final int batchSize;
   private final Duration lockTtl;
   private final LongSupplier monotonicMillis;

   @Autowired
   public EventCacheReconciler(
      EventReadModelRepository repository,
      RedisEventCacheWriter writer,
      StringRedisTemplate redis,
      Clock clock,
      @Value("${app.event-cache.reconcile.batch-size:100}") int batchSize,
      @Value("${app.event-cache.reconcile.lock-ttl:PT4M}") Duration lockTtl,
      @Value("${app.event-cache.reconcile.interval:PT5M}") Duration reconcileDelay
   ) {
       this(repository, writer, redis, clock, batchSize, lockTtl, reconcileDelay,
       () -> System.nanoTime() / 1_000_000L
       );
   }

    EventCacheReconciler(
            EventReadModelRepository repository,
            RedisEventCacheWriter writer,
            StringRedisTemplate redis,
            Clock clock,
            int batchSize,
            Duration lockTtl,
            Duration reconcileDelay,
            LongSupplier monotonicMillis) {
                if (batchSize <= 0) {
                     throw new IllegalArgumentException("batch size must be positive");
                }

                if (lockTtl == null || lockTtl.isZero() || lockTtl.isNegative()) {
                       throw new IllegalArgumentException("lock TTL must be positive");
                }

                if (reconcileDelay == null || reconcileDelay.isZero() || reconcileDelay.isNegative()) {
                       throw new IllegalArgumentException("reconcile delay must be positive");
                }

                this.repository = repository;
                this.writer = writer;
                this.redis = redis;
                this.clock = clock;
                this.batchSize = batchSize;
                this.lockTtl = lockTtl;
                this.monotonicMillis = monotonicMillis;
    }

    @Scheduled(fixedDelayString = "${app.event-cache.reconcile.interval:PT5M}")
    public void reconcile() {
    String token = UUID.randomUUID().toString();

    try {
    boolean lockAcquired = Boolean.TRUE.equals(
    redis.opsForValue().setIfAbsent(LOCK_KEY, token, lockTtl)
    );

    if (!lockAcquired) {
    return;
    }
    } catch (RuntimeException error) {
    log.error("Unable to acquire Redis reconciliation lock", error);
    return;
    }

    AtomicBoolean leaseLost = new AtomicBoolean();
    ScheduledExecutorService heartbeatExecutor = newHeartbeatExecutor();
    ScheduledFuture<?> heartbeat = null;

    try {
    if (!renew(token, leaseLost)) {
    return;
    }

    long heartbeatIntervalMillis = Math.max(1L, lockTtl.toMillis() / 3);
    long nextCooperativeRenewalAt = monotonicMillis.getAsLong() + heartbeatIntervalMillis;

    heartbeat = heartbeatExecutor.scheduleAtFixedRate(
    () -> renew(token, leaseLost),
    heartbeatIntervalMillis,
    heartbeatIntervalMillis,
    TimeUnit.MILLISECONDS
    );

    if (leaseLost.get()) {
    return;
    }

    try {
    writer.removeExpired();
    } catch (RuntimeException error) {
    log.error("Redis expired-index cleanup failed", error);
    }

    if (leaseLost.get()) {
    return;
    }

    OffsetDateTime after = clock.instant().atOffset(ZoneOffset.UTC);
    EventReadModelRepository.UpcomingCursor watermark;

    try {
    watermark = repository.findUpcomingWatermark(after).orElse(null);
    } catch (RuntimeException error) {
    log.error("Unable to capture event reconciliation watermark", error);
    return;
    }

    if (watermark == null || leaseLost.get()) {
    return;
    }

    EventReadModelRepository.UpcomingCursor cursor = null;

    while (!leaseLost.get()) {
    if (monotonicMillis.getAsLong() >= nextCooperativeRenewalAt) {
    if (!renew(token, leaseLost)) {
    return;
    }

    nextCooperativeRenewalAt = monotonicMillis.getAsLong() + heartbeatIntervalMillis;
    }

    List<com.bix.event_consumer.models.Event> batch;

    try {
    batch = repository.findUpcoming(after, cursor, watermark, batchSize);
    } catch (RuntimeException error) {
    log.error("Unable to read event reconciliation batch", error);
    return;
    }

    if (leaseLost.get()) {
    return;
    }

    for (var event : batch) {
    if (leaseLost.get()) {
    return;
    }

    if (monotonicMillis.getAsLong() >= nextCooperativeRenewalAt) {
    if (!renew(token, leaseLost)) {
    return;
    }

    nextCooperativeRenewalAt = monotonicMillis.getAsLong() + heartbeatIntervalMillis;
    }

    try {
    writer.refresh(event);
    } catch (RuntimeException error) {
    log.error("Redis reconciliation failed for event {}", event.getEventId(), error);
    }

    if (leaseLost.get()) {
    return;
    }
 }

 if (batch.size() < batchSize) {
 return;
 }

 var last = batch.getLast();
 cursor = new EventReadModelRepository.UpcomingCursor(
 last.getEventDate(),
 last.getEventId()
 );
 }
 } finally {
 if (heartbeat != null) {
 heartbeat.cancel(false);
 }

 heartbeatExecutor.shutdownNow();

 try {
 redis.execute(RELEASE, List.of(LOCK_KEY), token);
 } catch (RuntimeException error) {
 log.error("Unable to release Redis reconciliation lock", error);
 }
 }
 }

 private ScheduledExecutorService newHeartbeatExecutor() {
 ThreadFactory factory = Thread.ofPlatform()
 .daemon()
 .name("event-cache-reconcile-heartbeat-", 0)
 .factory();

 return Executors.newSingleThreadScheduledExecutor(factory);
 }

 private boolean renew(String token, AtomicBoolean leaseLost) {
 if (leaseLost.get()) {
 return false;
 }

 try {
 Long renewed = redis.execute(
 RENEW,
 List.of(LOCK_KEY),
 token,
 String.valueOf(lockTtl.toMillis())
 );

 if (!Long.valueOf(1L).equals(renewed)) {
 if (leaseLost.compareAndSet(false, true)) {
 log.warn("Redis reconciliation lease was lost; aborting run");
 }

 return false;
 }

 return true;
 } catch (RuntimeException error) {
 if (leaseLost.compareAndSet(false, true)) {
 log.error("Unable to renew Redis reconciliation lease; aborting run", error);
 }

 return false;
 }
 }
}
