package com.bix.event_consumer.rabbitmq;

public class RabbitPublishException extends IllegalStateException {
    public RabbitPublishException(String message, Throwable cause) {
        super(message, cause);
    }

    public RabbitPublishException(String message) {
        super(message);
    }
}
