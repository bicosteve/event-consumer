package com.bix.event_consumer.consumer;

import com.bix.event_consumer.services.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResultConsumer{
    private final ResultService resultService;

    @RabbitListener(queues = "${app.rabbitmq.results.queue}")
    public void consume(String eventId){
        log.info("ResultConsumer::received result trigger for event {}",eventId);
        this.resultService.processBetResults(eventId);
        try{
            log.info("Consumed event {} ", eventId);
        }catch(Exception e){
            log.error("ResultConsumer::error processing results for event {} : {} ", eventId,e.getMessage());
        }
    }
}
