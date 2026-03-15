package com.bix.event_consumer.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventStatus {
    STATUS_SCHEDULED(0),
    STATUS_IN_PROGRESS(1),
    STATUS_FINAL(2),
    STATUS_HALFTIME(3),
    STATUS_POSTPONED(4),
    STATUS_CANCELED(5),
    STATUS_DELAYED(6),
    STATUS_END_PERIOD(7);

    private final int code;


    @JsonCreator
    public static EventStatus fromValue(String value){
        for(EventStatus status : values()){
            if(status.name().equalsIgnoreCase(value)){
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown event status %s ".formatted(value));
    }

}
