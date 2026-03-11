package com.bix.event_consumer.consumer;

import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventsConsumer {

    private final EventRepository eventRepository;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consume(Event event){
        log.info("Consumer::received event {}",event.getEventId());
        try{
            log.info("Consumer::event details {}",event);
            this.eventRepository.updateEvent(event);
        }catch(Exception ex){
            log.error("Consumer::error processing event {}:{}",event.getEventId(), ex.getMessage());
        }
    }
}
