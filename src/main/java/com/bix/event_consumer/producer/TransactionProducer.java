package com.bix.event_consumer.producer;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.messaging.TransactionPublisher;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bix.event_consumer.rabbitmq.ConfirmingRabbitPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "rabbitmq")
public class TransactionProducer implements TransactionPublisher {
    private final ConfirmingRabbitPublisher confirmingRabbitPublisher;
    private final RabbitMQConfig rabbitMQConfig;

    @Override
    public void publish(BetStatusUpdate event) {
        confirmingRabbitPublisher.publish(
                rabbitMQConfig.getTransactions().getExchange(),
                rabbitMQConfig.getTransactions().getRoutingKey(),
                event
        );
        log.info("Published transaction event {}", event);
    }
}
