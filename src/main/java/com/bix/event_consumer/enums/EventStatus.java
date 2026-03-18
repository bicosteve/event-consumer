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
    STATUS_POSTPONED(3),
    STATUS_CANCELED(4),
    STATUS_SUSPENDED(5),
    STATUS_DELAYED(6),
    STATUS_RAIN_DELAY(7),
    STATUS_HALFTIME(8),
    STATUS_END_PERIOD(9),
    STATUS_END_OF_REGULATION(10),
    STATUS_OVERTIME(11),
    STATUS_FIRST_HALF(12),
    STATUS_SECOND_HALF(13),
    STATUS_FULL_TIME(14),
    STATUS_FINAL_AET(15),
    STATUS_FINAL_PEN(16),
    STATUS_SHOOTOUT(17),
    STATUS_FORFEIT(18);

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
