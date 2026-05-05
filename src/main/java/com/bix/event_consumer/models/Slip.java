package com.bix.event_consumer.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Slip{
    private Long            betSlipId;
    private Long            betId;
    private String          eventId;
    private Integer         sportId;
    private Integer         teamId;
    private Integer         marketId;
    private String          marketName;
    private String          participantName;
    private BigDecimal      odds;
    private String          specialBetValue;
    private Integer         status;
    private OffsetDateTime  createdAt;
    private OffsetDateTime  updatedAt;
}
