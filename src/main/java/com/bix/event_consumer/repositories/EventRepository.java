package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepository {
    private final JdbcTemplate jdbcTemplate;

    public void updateEvent(Event event){
        log.info("ConsumerRepo::Attempt to insert event {}",event.getEventId());
        log.info("ConsumerRepo::event {}",event);

    }
}
