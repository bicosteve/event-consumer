package com.bix.event_consumer.kafka;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.models.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "kafka")
public class KafkaMessagingConfiguration {
     private final KafkaMessagingConfig config;
     private final KafkaProperties kafkaProperties;
     private final ObjectMapper objectMapper;

     @Bean
     @ConditionalOnProperty(name = "app.messaging.kafka.create-topics", havingValue = "true")
     public NewTopic matchesTopic() {
       return new NewTopic(config.getMatchesTopic(), 3, (short) 1);
     }


     @Bean
     @ConditionalOnProperty(name = "app.messaging.kafka.create-topics", havingValue = "true")
     public NewTopic resultsTopic() {
       return new NewTopic(config.getResultsTopic(), 3, (short) 1);
     }


     @Bean
     @ConditionalOnProperty(name = "app.messaging.kafka.create-topics", havingValue = "true")
     public NewTopic transactionsTopic() {
       return new NewTopic(config.getTransactionsTopic(), 3, (short) 1);
     }


     @Bean
     @ConditionalOnProperty(name = "app.messaging.kafka.create-topics", havingValue = "true")
     public NewTopic matchesDltTopic() {
         return new NewTopic(config.getMatchesTopic() + ".DLT", 3, (short) 1);
     }


     @Bean
     @ConditionalOnProperty(name = "app.messaging.kafka.create-topics", havingValue = "true")
     public NewTopic resultsDltTopic() {
        return new NewTopic(config.getResultsTopic() + ".DLT", 3, (short) 1);
     }


     @Bean
     @ConditionalOnProperty(name = "app.messaging.kafka.create-topics", havingValue = "true")
     public NewTopic transactionsDltTopic() {
        return new NewTopic(config.getTransactionsTopic() + ".DLT", 3, (short) 1);
     }

     @Bean
     public KafkaTemplate<String, Object> kafkaTemplate() {
     Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), dltCapableValueSerializer()));
     }

    @Bean
    public KafkaTemplate<String, String> resultsKafkaTemplate() {
    Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
       return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), new StringSerializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Event> matchesKafkaListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
       return factory(Event.class, kafkaTemplate);
     }

     @Bean
     public ConcurrentKafkaListenerContainerFactory<String, String> resultsKafkaListenerContainerFactory(KafkaTemplate<String, String> resultsKafkaTemplate) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildConsumerProperties());

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties, errorHandlingKeyDeserializer(), errorHandlingValueDeserializer(new StringDeserializer())));
        factory.setCommonErrorHandler(errorHandler(resultsKafkaTemplate));

        return factory;
     }


     @Bean
     public ConcurrentKafkaListenerContainerFactory<String, BetStatusUpdate> transactionsKafkaListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        return factory(BetStatusUpdate.class, kafkaTemplate);
     }

     private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> valueType, KafkaTemplate<String, Object> kafkaTemplate) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildConsumerProperties());

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(valueType, objectMapper, false);

        deserializer.addTrustedPackages("com.bix.event_consumer.*");

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties, errorHandlingKeyDeserializer(), errorHandlingValueDeserializer(deserializer)));
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));

        return factory;
     }

    private ErrorHandlingDeserializer<String> errorHandlingKeyDeserializer() {
       return new ErrorHandlingDeserializer<>(new StringDeserializer());
    }

    private <T> ErrorHandlingDeserializer<T> errorHandlingValueDeserializer(org.apache.kafka.common.serialization.Deserializer<T> delegate) {
       return new ErrorHandlingDeserializer<>(delegate);
    }

    private Serializer<Object> dltCapableValueSerializer() {
      return new DelegatingByTypeSerializer(Map.of(
        byte[].class, new ByteArraySerializer(),
        Object.class, new JsonSerializer<>(objectMapper)), true);
    }

    private DefaultErrorHandler errorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
       return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate,
         (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())), new FixedBackOff(1_000L, 2L));
    }

    @Bean public KafkaJsonPublisher kafkaJsonPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaTemplate<String, String> resultsKafkaTemplate) {
         return new KafkaJsonPublisher(kafkaTemplate, resultsKafkaTemplate, config);
     }
}
