package com.bix.event_consumer.consumers;

import com.bix.event_consumer.messaging.ResultMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "rabbitmq")
public class ResultConsumer {
    private final ResultMessageHandler resultMessageHandler;

    @RabbitListener(queues = "${app.messaging.rabbitmq.results.queue}")
    public void consume(String eventId) {
        resultMessageHandler.handle(eventId);
    }
}
