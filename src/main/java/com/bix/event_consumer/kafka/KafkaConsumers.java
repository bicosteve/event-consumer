package com.bix.event_consumer.kafka;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.messaging.EventMessageHandler;
import com.bix.event_consumer.messaging.ResultMessageHandler;
import com.bix.event_consumer.messaging.TransactionMessageHandler;
import com.bix.event_consumer.models.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "kafka")
public class KafkaConsumers {
    private final EventMessageHandler eventMessageHandler;
    private final ResultMessageHandler resultMessageHandler;
    private final TransactionMessageHandler transactionMessageHandler;

    @KafkaListener(topics = "${app.messaging.kafka.matches-topic}", groupId = "${app.messaging.kafka.matches-group}", containerFactory = "matchesKafkaListenerContainerFactory")
    public void consumeMatch(Event event) {
        eventMessageHandler.handle(event);
    }

    @KafkaListener(topics = "${app.messaging.kafka.results-topic}", groupId = "${app.messaging.kafka.results-group}", containerFactory = "resultsKafkaListenerContainerFactory")
    public void consumeResult(String eventId) {
        resultMessageHandler.handle(eventId);
    }

    @KafkaListener(topics = "${app.messaging.kafka.transactions-topic}", groupId = "${app.messaging.kafka.transactions-group}", containerFactory = "transactionsKafkaListenerContainerFactory")
    public void consumeTransaction(BetStatusUpdate update) {
        transactionMessageHandler.handle(update);
    }
}
