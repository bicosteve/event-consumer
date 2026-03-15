package com.bix.event_consumer.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {
    // This should be the odds table
    @JsonIgnore
    private Integer priceId; // set by the table

    private String id;
    private int price;

    private Integer priceDelta;

    @JsonProperty("is_main_line")
    private boolean isMainLine;

    @JsonIgnore
    public BigDecimal getOdds(){
        if(this.price >= 0){
            return BigDecimal.valueOf(this.price)
                    .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP)
                    .add(BigDecimal.ONE);
        } else {
            return BigDecimal.valueOf(100)
                    .divide(BigDecimal.valueOf(Math.abs(this.price)), 2,RoundingMode.HALF_UP)
                    .add(BigDecimal.ONE);
        }

    }

    @JsonIgnore
    private Integer participantId;
    @JsonIgnore
    private Integer bookMarkerId; // this is the price key
    @JsonIgnore
    private String handicapValue;

    @JsonIgnore
    private String lineId;

    @JsonIgnore
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime closedAt;


}
