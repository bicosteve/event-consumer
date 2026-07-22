package com.bix.event_consumer.consumers;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.messaging.TransactionMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "rabbitmq")
public class TransactionsConsumer {
    private final TransactionMessageHandler transactionMessageHandler;

    @RabbitListener(queues = "${app.messaging.rabbitmq.transactions.queue}")
    public void consume(BetStatusUpdate update) {
        transactionMessageHandler.handle(update);
    }
}
