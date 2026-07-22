package com.bix.event_consumer.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class ConfirmingRabbitPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final Duration timeout;

    public void publish(String exchange, String routingKey, Object payload) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload, correlationData);
            CorrelationData.Confirm confirm = correlationData.getFuture().get(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!confirm.isAck()) {
                throw new RabbitPublishException("RabbitMQ publisher nack for exchange " + exchange + ": " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new RabbitPublishException("RabbitMQ returned unroutable message for exchange " + exchange + " and routing key " + routingKey);
            }
        } catch (TimeoutException exception) {
            throw new RabbitPublishException("Timed out waiting for RabbitMQ publisher confirm", exception);
        } catch (RabbitPublishException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RabbitPublishException("Interrupted waiting for RabbitMQ publisher confirm", exception);
        } catch (Exception exception) {
            throw new RabbitPublishException("RabbitMQ publish failed", exception);
        }
    }
}
