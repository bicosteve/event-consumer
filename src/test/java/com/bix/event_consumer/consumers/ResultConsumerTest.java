package com.bix.event_consumer.consumers;

import com.bix.event_consumer.services.ResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResultConsumerTest {

    @Mock
    private ResultService resultService;

    private ResultConsumer resultConsumer;

    @BeforeEach
    void setUp() {
        resultConsumer = new ResultConsumer(resultService);
    }

    @Test
    void testConsumeCallsResultService() {
        resultConsumer.consume("evt-1");

        verify(resultService, times(1)).processBetResults("evt-1");
    }

    @Test
    void testConsumeCatchesExceptionFromResultService() {
        doThrow(new RuntimeException("oops"))
                .when(resultService).processBetResults("evt-1");

        // Should not throw
        resultConsumer.consume("evt-1");

        verify(resultService, times(1)).processBetResults("evt-1");
    }

    @Test
    void testConsumeWithNullEventId() {
        resultConsumer.consume(null);

        verify(resultService, times(1)).processBetResults(null);
    }
}
