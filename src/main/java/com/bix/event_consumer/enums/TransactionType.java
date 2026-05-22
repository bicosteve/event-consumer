package com.bix.event_consumer.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    CREDIT(1),
    DEBIT(2),
    REFUND(3);

    private final int status;

    @JsonCreator
    public static TransactionType fromValue(String value){
        for(TransactionType type : values()){
            if(type.name().equalsIgnoreCase(value)){
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type %s ".formatted(value));
    }
}
