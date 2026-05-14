package com.bix.event_consumer.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SlipStatus{
    PENDING(1),
    LOST(3),
    WON(5),
    VOID(7);

    private final int status;

    @JsonCreator
    public static SlipStatus fromValue(String value){
        for(SlipStatus status : values()){
            if(status.name().equalsIgnoreCase(value)){
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown slip status %s ".formatted(value));
    }
}
