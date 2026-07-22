package com.bix.event_consumer.rabbitmq;

import com.bix.event_consumer.messaging.ResultTriggerPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "rabbitmq")
public class RabbitResultTriggerPublisher implements ResultTriggerPublisher {
private final ConfirmingRabbitPublisher confirmingRabbitPublisher;
private final RabbitMQConfig config;

    @Override
    public void publish(String eventId) {
        confirmingRabbitPublisher.publish(
                config.getResults().getExchange(),
                config.getResults().getRoutingKey(),
                eventId
        );
    }
}
