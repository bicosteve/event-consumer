package com.bix.event_consumer.messaging;

import com.bix.event_consumer.services.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResultMessageHandler {
    private final ResultService resultService;

    public void handle(String eventId) {
        resultService.processBetResults(eventId);
    }
}
