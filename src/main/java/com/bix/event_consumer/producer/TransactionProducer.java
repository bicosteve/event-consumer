package com.bix.event_consumer.producer;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    public void publish(BetStatusUpdate event) {
        this.rabbitTemplate.convertAndSend(
                this.rabbitMQConfig.getTransactions().getExchange(),
                this.rabbitMQConfig.getTransactions().getRoutingKey(),
                event
        );

        log.info("Publish transaction event {}", event);
    }
}
