package com.bix.event_consumer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {
    // This should be the odds table
    private Integer oddsId; // set by the table
    private String id;
    private int price;
    private int priceDelta;

    @JsonProperty("is_main_line")
    private boolean isMainLine;

    private BigDecimal odds; // calculated and set in respect to price

    private Integer participantId;
    private Integer bookMarkerId; // this is the price key
    private String handicapValue;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;


}
