package com.bix.event_consumer.kafka;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.models.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.SerializationUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaHeaderlessJsonDeserializationTest {
 private final ObjectMapper objectMapper = new com.bix.event_consumer.config.JacksonConfig().objectMapper();

 @Test
 void deserializesRapidEngineMatchPayloadWithoutSpringTypeHeaders() {
 JsonDeserializer<Event> deserializer = new JsonDeserializer<>(Event.class, objectMapper, false);
 String payload = """
 {"event_id":"sr:match:991","event_uuid":"9f75f2d1-0e6c-4abb-8d2b-e982c0e348b0","sport_id":1,"event_date":"2026-07-21T15:30:00Z","score":{"event_id":"sr:match:991","event_status":"STATUS_FINAL","event_status_detail":"Final","team_id_away":101,"team_id_home":202,"winner_away":1,"winner_home":0,"score_away":2,"score_home":1}}
 """;

 Event event = deserializer.deserialize("matches.queue", payload.getBytes(StandardCharsets.UTF_8));

 assertEquals("sr:match:991", event.getEventId());
 assertEquals("9f75f2d1-0e6c-4abb-8d2b-e982c0e348b0", event.getEventUuid());
 assertEquals(2, event.getScore().getScoreAway());
 assertEquals("STATUS_FINAL", event.getScore().getEventStatus().name());
 }

 @Test
 void deserializesRapidEngineTransactionPayloadWithoutSpringTypeHeaders() {
 JsonDeserializer<BetStatusUpdate> deserializer = new JsonDeserializer<>(BetStatusUpdate.class, objectMapper, false);
 String payload = """
 {"bet_id":42,"profile_id":7,"amount":12.50,"previous_status":1,"current_status":5,"possible_win":25.00,"reference":"ignored-upstream-reference","update_at":"2026-07-21T15:30:00"}
 """;

 BetStatusUpdate update = deserializer.deserialize("transactions.queue", payload.getBytes(StandardCharsets.UTF_8));

 assertEquals(42L, update.getBetId());
 assertEquals(7L, update.getProfileId());
 assertEquals(5, update.getCurrentStatus());
    assertEquals("ignored-upstream-reference", update.getReference());
    }

    @Test
    void configuredMatchesFactoryRoutesMalformedJsonThroughErrorHandlingDeserializerAndDltRecoverer() {
        KafkaMessagingConfiguration configuration = configuration();
        KafkaTemplate<String, Object> dltTemplate = mock(KafkaTemplate.class);

        ConcurrentKafkaListenerContainerFactory<String, Event> factory = configuration.matchesKafkaListenerContainerFactory(dltTemplate);
        Object valueDeserializer = factory.getConsumerFactory().getValueDeserializer();
        Object errorHandler = ReflectionTestUtils.getField(factory, "commonErrorHandler");

        ErrorHandlingDeserializer<Event> errorHandlingDeserializer = assertInstanceOf(ErrorHandlingDeserializer.class, valueDeserializer);
        RecordHeaders headers = new RecordHeaders();
        byte[] malformedPayload = "{not-json".getBytes(StandardCharsets.UTF_8);

 assertNull(errorHandlingDeserializer.deserialize("matches.queue", headers, malformedPayload));
 DefaultErrorHandler configuredErrorHandler = assertInstanceOf(DefaultErrorHandler.class, errorHandler);
 ConsumerRecord<String, Event> malformedRecord = new ConsumerRecord<>("matches.queue", 0, 0L, 0L, TimestampType.CREATE_TIME,
                3, malformedPayload.length, "key", null, headers, java.util.Optional.empty());
        assertInstanceOf(org.springframework.kafka.support.serializer.DeserializationException.class,
                SerializationUtils.getExceptionFromHeader(malformedRecord,
                        SerializationUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER, null));
        assertArrayEquals(malformedPayload, dltValueSerializer(configuration).serialize("matches.queue.DLT", malformedPayload));
        when(dltTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
        DeadLetterPublishingRecoverer recoverer = recoverer(configuredErrorHandler);
        recoverer.accept(malformedRecord, null, new IllegalStateException("malformed JSON"));
        ArgumentCaptor<ProducerRecord> recoveredRecord = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(dltTemplate).send(recoveredRecord.capture());
        assertArrayEquals(malformedPayload, (byte[]) recoveredRecord.getValue().value());
        assertInstanceOf(ErrorHandlingDeserializer.class,
                configuration.transactionsKafkaListenerContainerFactory(dltTemplate).getConsumerFactory().getValueDeserializer());
    }

    @Test
    void configuredResultsRecovererUsesStringTemplateAndPreservesUnquotedPayloadBytes() {
        KafkaMessagingConfiguration configuration = configuration();
        KafkaTemplate<String, String> resultsTemplate = mock(KafkaTemplate.class);
        byte[] payload = "event-42:final".getBytes(StandardCharsets.UTF_8);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("results.queue", 1, 3L, "event-42:final", "event-42:final");

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                configuration.resultsKafkaListenerContainerFactory((KafkaTemplate) resultsTemplate);
        DefaultErrorHandler errorHandler = (DefaultErrorHandler) ReflectionTestUtils.getField(factory, "commonErrorHandler");
        DeadLetterPublishingRecoverer recoverer = recoverer(errorHandler);
        Object templateResolver = ReflectionTestUtils.getField(recoverer, "templateResolver");
        Object selectedTemplate = ((java.util.function.Function) templateResolver)
 .apply(new org.apache.kafka.clients.producer.ProducerRecord<>("results.queue.DLT", record.key(), record.value()));

 assertArrayEquals(payload, resultsValueSerializer(configuration).serialize("results.queue.DLT", record.value()));
        assertEquals(resultsTemplate, selectedTemplate);
    }

    private DeadLetterPublishingRecoverer recoverer(DefaultErrorHandler errorHandler) {
        Object failureTracker = ReflectionTestUtils.getField(errorHandler, "failureTracker");
        return (DeadLetterPublishingRecoverer) ReflectionTestUtils.invokeMethod(failureTracker, "getRecoverer");
    }

    private Serializer<Object> dltValueSerializer(KafkaMessagingConfiguration configuration) {
        DefaultKafkaProducerFactory<String, Object> producerFactory = (DefaultKafkaProducerFactory<String, Object>)
                configuration.kafkaTemplate().getProducerFactory();
        return producerFactory.getValueSerializer();
    }

    private Serializer<String> resultsValueSerializer(KafkaMessagingConfiguration configuration) {
        DefaultKafkaProducerFactory<String, String> producerFactory = (DefaultKafkaProducerFactory<String, String>)
                configuration.resultsKafkaTemplate().getProducerFactory();
        return producerFactory.getValueSerializer();
    }

    private KafkaMessagingConfiguration configuration() {
        KafkaMessagingConfig config = new KafkaMessagingConfig();
 config.setMatchesTopic("matches.queue");
 config.setResultsTopic("results.queue");
 config.setTransactionsTopic("transactions.queue");
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers(java.util.List.of("localhost:9092"));
        return new KafkaMessagingConfiguration(config, kafkaProperties, objectMapper);
    }
}

