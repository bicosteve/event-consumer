package com.bix.event_consumer.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCacheService {
    private final EventReadModelRepository repository;
    private final RedisEventCacheWriter writer;

    public void refreshAfterCommit(String eventId) {
        Runnable refresh = () -> refreshSafely(eventId);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { refresh.run(); }
            });
        } else {
            refresh.run();
        }
    }

    public void refreshSafely(String eventId) {
        try {
            repository.findByEventId(eventId).ifPresent(writer::refresh);
        } catch (RuntimeException error) {
            log.error("Redis event cache refresh failed for event {}:{}", eventId, error.getMessage(), error);
        }
    }
}
