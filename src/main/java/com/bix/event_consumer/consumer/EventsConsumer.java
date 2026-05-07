package com.bix.event_consumer.consumer;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import com.bix.event_consumer.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventsConsumer {
    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    @RabbitListener(queues = "${app.rabbitmq.matches.queue}")
    public void consume(Event event){
        log.info("Consumer::received event {}",event.getEventId());
        try{
            // 01. Store the event on the events table
            this.eventRepository.updateEvent(event);

            // 02. Check if the status of the game is final
            // Publish the score to the results queue
            if(this.isFinalStatus(event.getScore())){
                log.info("EventConsumer::event {} is finished. Publishing scores to results queue",event.getEventId());
                this.rabbitTemplate.convertAndSend(
                        this.rabbitMQConfig.getResults().getExchange(),
                        this.rabbitMQConfig.getResults().getRoutingKey(),
                        event.getEventId()
                );
            }

        }catch(Exception ex){
            log.error("Consumer::error processing event {}:{}",event.getEventId(), ex.getMessage(), ex);
        }
    }

    private boolean isFinalStatus(Score score){
        // Check what is in score if null return;
        // If not null, check if eventStatus is 2 (FINAL)
        if(score == null) return false;

        return score.getEventStatus() == EventStatus.STATUS_FINAL
                || score.getEventStatus() == EventStatus.STATUS_FULL_TIME
                || score.getEventStatus() == EventStatus.STATUS_FINAL_PEN
                || score.getEventStatus() == EventStatus.STATUS_FINAL_AET;
    }
}
