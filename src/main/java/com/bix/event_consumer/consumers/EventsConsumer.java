package com.bix.event_consumer.consumers;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import com.bix.event_consumer.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventsConsumer {
    private final EventService eventService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    @RabbitListener(queues = "${app.rabbitmq.matches.queue}")
    public void consume(Event event){
        log.info("Received event {}",event.getEventId());
        try{
            // 01. Send the event to the eventService
            // this will then be sent to eventRepository
            this.eventService.consumeEvents(event);

 // 02. Publish final or void events to the results queue.
 if(this.requiresResultProcessing(event.getScore())){
 log.info("Event={} requires result processing. Publishing scores to results queue",event.getEventId());
                this.rabbitTemplate.convertAndSend(
                        this.rabbitMQConfig.getResults().getExchange(),
                        this.rabbitMQConfig.getResults().getRoutingKey(),
                        event.getEventId()
                );
            }

        }catch(Exception ex){
            log.error("Error processing event {}:{}",event.getEventId(), ex.getMessage(), ex);
        }
    }

 private boolean requiresResultProcessing(Score score){
 if(score == null) return false;

 return isFinalStatus(score.getEventStatus()) || isVoidStatus(score.getEventStatus());
 }

 private boolean isFinalStatus(EventStatus status){
 return status == EventStatus.STATUS_FINAL
 || status == EventStatus.STATUS_FULL_TIME
 || status == EventStatus.STATUS_FINAL_PEN
 || status == EventStatus.STATUS_FINAL_AET;
 }

 private boolean isVoidStatus(EventStatus status){
 return status == EventStatus.STATUS_POSTPONED
 || status == EventStatus.STATUS_CANCELED
 || status == EventStatus.STATUS_SUSPENDED
 || status == EventStatus.STATUS_FORFEIT
 || status == EventStatus.STATUS_RETIRED
 || status == EventStatus.STATUS_UNKNOWN;
 }
}
