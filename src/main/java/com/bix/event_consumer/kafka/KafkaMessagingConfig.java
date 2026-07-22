package com.bix.event_consumer.kafka;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.messaging.kafka")
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "kafka")
public class KafkaMessagingConfig {
    @NotBlank
    private String matchesTopic;

    @NotBlank
    private String resultsTopic;

    @NotBlank
    private String transactionsTopic;

    @NotBlank
    private String matchesGroup;

    @NotBlank
    private String resultsGroup;

    @NotBlank
    private String transactionsGroup;

    private boolean createTopics;
}
