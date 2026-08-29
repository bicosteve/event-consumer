package com.bix.event_consumer.services;

import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.repositories.EventRepository;
import com.bix.event_consumer.cache.EventCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {
private final EventRepository eventRepository;
private final EventCacheService eventCacheService;

@Transactional
public EventPersistenceOutcome consumeEvents(Event event){
        if(event == null){
            log.warn("Event is null. Skipping...");
            return EventPersistenceOutcome.SKIPPED;
        }

EventPersistenceOutcome outcome = this.eventRepository.updateEvent(event);
        if (outcome == EventPersistenceOutcome.PERSISTED) {
            this.eventCacheService.refreshAfterCommit(event.getEventId());
        }

        return outcome;
    }
}
