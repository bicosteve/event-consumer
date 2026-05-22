package com.bix.event_consumer.events;

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
public class BetStatusUpdate {
    private Long betId;
    private Long profileId;
    private BigDecimal amount;
    private Integer previousStatus;
    private Integer currentStatus;
    private BigDecimal possibleWin;
    private Integer type;
    private String reference;
    private LocalDateTime updateAt;
}
