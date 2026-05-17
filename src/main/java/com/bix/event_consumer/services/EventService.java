package com.bix.event_consumer.services;

import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public void publishEvents(Event event){
        // Validate the incoming event
        if(event == null){
            log.warn("Event is null. Skipping...");
            return;
        }

        this.eventRepository.updateEvent(event);
    }
}
