package com.bix.event_consumer.services;

import com.bix.event_consumer.cache.EventCacheService;
import com.bix.event_consumer.cache.EventReadModelRepository;
import com.bix.event_consumer.cache.RedisEventCacheWriter;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.repositories.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EventServiceTest {
    private final EventRepository repository = mock(EventRepository.class);
    private final EventReadModelRepository readModel = mock(EventReadModelRepository.class);
    private final RedisEventCacheWriter writer = mock(RedisEventCacheWriter.class);
    private final EventCacheService cache = new EventCacheService(readModel, writer);
    private final RecordingTransactionManager transactionManager = new RecordingTransactionManager();
    private final EventService service = transactionalService();

    @Test
    void nullEventIsSkippedWithoutPersistenceOrCacheRefresh() {
        assertEquals(EventPersistenceOutcome.SKIPPED, service.consumeEvents(null));
        verifyNoInteractions(repository, readModel, writer);
    }

    @Test
    void persistedEventRefreshesOnlyAfterCommit() {
        Event event = Event.builder().eventId("evt-1").build();
        Event persisted = Event.builder().eventId("evt-1").build();
        when(repository.updateEvent(event)).thenAnswer(invocation -> {
            verifyNoInteractions(readModel, writer);
            return EventPersistenceOutcome.PERSISTED;
        });
        when(readModel.findByEventId("evt-1")).thenReturn(Optional.of(persisted));

        assertEquals(EventPersistenceOutcome.PERSISTED, service.consumeEvents(event));

        verify(writer).refresh(persisted);
        assertEquals(1, transactionManager.commits);
        assertEquals(0, transactionManager.rollbacks);
    }

    @Test
    void persistedEventCallbackDoesNotRunOnRollback() {
        Event event = Event.builder().eventId("evt-1").build();
        when(repository.updateEvent(event)).thenAnswer(invocation -> {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return EventPersistenceOutcome.PERSISTED;
        });

        assertEquals(EventPersistenceOutcome.PERSISTED, service.consumeEvents(event));

        verifyNoInteractions(readModel, writer);
        assertEquals(0, transactionManager.commits);
        assertEquals(1, transactionManager.rollbacks);
    }

    @Test
    void skippedEventDoesNotScheduleCacheRefresh() {
        Event event = Event.builder().eventId("evt-1").build();
        when(repository.updateEvent(event)).thenReturn(EventPersistenceOutcome.SKIPPED);

        assertEquals(EventPersistenceOutcome.SKIPPED, service.consumeEvents(event));

        verifyNoInteractions(readModel, writer);
    }

    private EventService transactionalService() {
        ProxyFactory proxyFactory = new ProxyFactory(new EventService(repository, cache));
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager, new AnnotationTransactionAttributeSource()));
        return (EventService) proxyFactory.getProxy();
    }

    private static final class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        private int commits;
        private int rollbacks;

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            commits++;
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rollbacks++;
        }
    }
}
