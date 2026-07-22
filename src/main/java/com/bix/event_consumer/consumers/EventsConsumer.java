package com.bix.event_consumer.consumers;

import com.bix.event_consumer.messaging.EventMessageHandler;
import com.bix.event_consumer.models.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "rabbitmq")
public class EventsConsumer {
    private final EventMessageHandler eventMessageHandler;

    @RabbitListener(queues = "${app.messaging.rabbitmq.matches.queue}")
    public void consume(Event event) {
        eventMessageHandler.handle(event);
    }
}
