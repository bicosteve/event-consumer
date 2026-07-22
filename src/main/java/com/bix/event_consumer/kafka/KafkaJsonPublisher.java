package com.bix.event_consumer.kafka;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.messaging.ResultTriggerPublisher;
import com.bix.event_consumer.messaging.TransactionPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class KafkaJsonPublisher implements ResultTriggerPublisher, TransactionPublisher {
   private final KafkaTemplate<String, Object> kafkaTemplate;
   private final KafkaTemplate<String, String> resultsKafkaTemplate;
   private final KafkaMessagingConfig config;

   @Override
   public void publish(String eventId) {
     try {
         resultsKafkaTemplate
                 .send(config.getResultsTopic(), eventId, eventId)
                 .get(10, TimeUnit.SECONDS);
     } catch (Exception exception) {
         throw new IllegalStateException("Kafka publish failed for topic " + config.getResultsTopic(), exception);
     }
   }

    @Override
    public void publish(BetStatusUpdate update) {
       sendAndAwait(config.getTransactionsTopic(), String.valueOf(update.getBetId()), update);
    }

    private void sendAndAwait(String topic, String key, Object value) {
       try {
           kafkaTemplate.send(topic, key, value).get(10, TimeUnit.SECONDS);
       } catch (Exception exception) {
           throw new IllegalStateException("Kafka publish failed for topic " + topic, exception);
       }
    }
}
