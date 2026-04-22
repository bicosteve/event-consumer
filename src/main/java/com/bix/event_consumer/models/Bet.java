package com.bix.event_consumer.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bet{
    private Long                betId;
    private Long                profileId;
    private BigDecimal          stake;
    private Integer             isBonus;
    private Integer             status;
    private BigDecimal          totalOdds;
    private BigDecimal          possibleWin;
    private LocalDateTime       createdAt;
    private LocalDateTime       updatedAt;

    List<Slip> slips;

}
