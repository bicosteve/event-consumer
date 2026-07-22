package com.bix.event_consumer.messaging;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventMessageHandler {
    private final EventService eventService;
    private final ResultTriggerPublisher resultTriggerPublisher;

    public void handle(Event event) {
if (eventService.consumeEvents(event) == com.bix.event_consumer.services.EventPersistenceOutcome.PERSISTED
&& requiresResultProcessing(event.getScore())) {
            resultTriggerPublisher.publish(event.getEventId());
        }
    }

    private boolean requiresResultProcessing(Score score) {
        if (score == null) {
            return false;
        }

        EventStatus status = score.getEventStatus();

        return status == EventStatus.STATUS_FINAL
                || status == EventStatus.STATUS_FULL_TIME
                || status == EventStatus.STATUS_FINAL_PEN
                || status == EventStatus.STATUS_FINAL_AET
                || status == EventStatus.STATUS_POSTPONED
                || status == EventStatus.STATUS_CANCELED
                || status == EventStatus.STATUS_SUSPENDED
                || status == EventStatus.STATUS_FORFEIT
                || status == EventStatus.STATUS_RETIRED
                || status == EventStatus.STATUS_UNKNOWN;
    }
}
