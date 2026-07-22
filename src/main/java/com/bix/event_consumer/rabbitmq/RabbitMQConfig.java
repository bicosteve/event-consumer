package com.bix.event_consumer.rabbitmq;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.messaging.rabbitmq")
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "rabbitmq")
public class RabbitMQConfig {
@Valid private QueueConfig matches;
@Valid private QueueConfig results;
@Valid private QueueConfig transactions;
private long publisherTimeoutMillis = 10_000L;

@Data
 public static class QueueConfig {
@NotBlank private String exchange;
@NotBlank private String queue;
@NotBlank private String routingKey;
@NotBlank private String deadLetterExchange;
@NotBlank private String deadLetterRoutingKey;
@NotBlank private String deadLetterQueue;
 }
}
