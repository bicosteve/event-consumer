package com.bix.event_consumer.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.messaging")
public class MessagingBrokerProperties {
    @NotBlank(message = "MESSAGING_BROKER is required")
    @Pattern(regexp = "kafka|rabbitmq", message = "MESSAGING_BROKER must be one of: kafka, rabbitmq")
    private String broker;
}
