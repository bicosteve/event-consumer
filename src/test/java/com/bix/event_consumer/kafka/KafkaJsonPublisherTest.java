package com.bix.event_consumer.kafka;

import com.bix.event_consumer.events.BetStatusUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaJsonPublisherTest {
@Mock
private KafkaTemplate<String, Object> kafkaTemplate;
@Mock
private KafkaTemplate<String, String> resultsKafkaTemplate;

private KafkaJsonPublisher publisher;

    @BeforeEach
    void setUp() {
        KafkaMessagingConfig config = new KafkaMessagingConfig();
 config.setResultsTopic("results.queue");
 config.setTransactionsTopic("transactions.queue");
publisher = new KafkaJsonPublisher(kafkaTemplate, resultsKafkaTemplate, config);
    }

    @Test
void keysResultTriggerByEventId() {
when(resultsKafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

publisher.publish("event-42");

        verify(resultsKafkaTemplate).send("results.queue", "event-42", "event-42");
    }

 @Test
void keysTransactionUpdateByBetId() {
when(kafkaTemplate.send(anyString(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
BetStatusUpdate update = BetStatusUpdate.builder().betId(42L).build();

 publisher.publish(update);

 verify(kafkaTemplate).send("transactions.queue", "42", update);
 }

 @Test
 void propagatesAsynchronousKafkaSendFailuresBeforeReturning() {
 CompletableFuture failedSend = new CompletableFuture<>();
 failedSend.completeExceptionally(new IllegalStateException("broker unavailable"));
 when(resultsKafkaTemplate.send("results.queue", "event-42", "event-42")).thenReturn(failedSend);

assertThrows(IllegalStateException.class, () -> publisher.publish("event-42"));
}

@Test
void resultEventIdRoundTripsAsExactUnquotedStringBytes() {
String eventId = "event-42:final";
StringSerializer serializer = new StringSerializer();
StringDeserializer deserializer = new StringDeserializer();

byte[] payload = serializer.serialize("results", eventId);

assertEquals(eventId, new String(payload, StandardCharsets.UTF_8));
assertEquals(eventId, deserializer.deserialize("results", payload));
}
}
