package com.bix.event_consumer.messaging;

import com.bix.event_consumer.kafka.KafkaConsumers;
import com.bix.event_consumer.kafka.KafkaMessagingConfig;
import com.bix.event_consumer.kafka.KafkaMessagingConfiguration;
import com.bix.event_consumer.kafka.KafkaJsonPublisher;
import com.bix.event_consumer.rabbitmq.RabbitMQBeans;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import com.bix.event_consumer.rabbitmq.RabbitResultTriggerPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagingBrokerPropertiesTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsKafkaAndRabbitMq() {
        MessagingBrokerProperties kafka = properties("kafka");
        MessagingBrokerProperties rabbit = properties("rabbitmq");

        assertTrue(validator.validate(kafka).isEmpty());
        assertTrue(validator.validate(rabbit).isEmpty());
    }

    @Test
    void rejectsMissingBroker() {
        MessagingBrokerProperties properties = properties(null);

        assertTrue(validator.validate(properties).stream()
                .anyMatch(violation -> violation.getMessage().contains("required")));
    }

    @Test
    void rejectsUnsupportedBroker() {
        MessagingBrokerProperties properties = properties("pulsar");

        assertEquals(1, validator.validate(properties).size());
    }

    private MessagingBrokerProperties properties(String broker) {
        MessagingBrokerProperties properties = new MessagingBrokerProperties();
        properties.setBroker(broker);
        return properties;
    }
}
