package com.bix.event_consumer.consumers;

import com.bix.event_consumer.messaging.ResultMessageHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResultConsumerTest {
    @Mock
    private ResultMessageHandler resultMessageHandler;

    @Test
    void delegatesResultTriggerToSharedHandler() {
        ResultConsumer consumer = new ResultConsumer(resultMessageHandler);

        consumer.consume("evt-1");

        verify(resultMessageHandler).handle("evt-1");
    }

    @Test
    void propagatesHandlerFailureForBrokerRetry() {
        RuntimeException failure = new RuntimeException("settlement unavailable");
        doThrow(failure).when(resultMessageHandler).handle("evt-1");
        ResultConsumer consumer = new ResultConsumer(resultMessageHandler);

        assertThrows(RuntimeException.class, () -> consumer.consume("evt-1"));
    }
}
