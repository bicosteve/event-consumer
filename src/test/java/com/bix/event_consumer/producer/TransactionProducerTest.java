package com.bix.event_consumer.producer;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQConfig rabbitMQConfig;

    @Mock
    private RabbitMQConfig.QueueConfig transactionsConfig;

    private TransactionProducer transactionProducer;

    @BeforeEach
    void setUp() {
        transactionProducer = new TransactionProducer(rabbitTemplate, rabbitMQConfig);
    }

    @Test
    void testPublishSendsMessage() {
        BetStatusUpdate event = BetStatusUpdate.builder()
                .betId(1L)
                .build();

        when(rabbitMQConfig.getTransactions()).thenReturn(transactionsConfig);
        when(transactionsConfig.getExchange()).thenReturn("transactions.exchange");
        when(transactionsConfig.getRoutingKey()).thenReturn("transactions.key");

        transactionProducer.publish(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend("transactions.exchange", "transactions.key", event);
    }

    @Test
    void testPublishWithMultipleEvents() {
        BetStatusUpdate event1 = BetStatusUpdate.builder().betId(1L).build();
        BetStatusUpdate event2 = BetStatusUpdate.builder().betId(2L).build();

        when(rabbitMQConfig.getTransactions()).thenReturn(transactionsConfig);
        when(transactionsConfig.getExchange()).thenReturn("transactions.exchange");
        when(transactionsConfig.getRoutingKey()).thenReturn("transactions.key");

        transactionProducer.publish(event1);
        transactionProducer.publish(event2);

        verify(rabbitTemplate, times(2))
                .convertAndSend(eq("transactions.exchange"), eq("transactions.key"),
                        any(BetStatusUpdate.class));
    }
}
