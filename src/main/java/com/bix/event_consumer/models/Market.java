package com.bix.event_consumer.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Market {
    // This should be the market table
    private Long                id;
    private Integer             marketId;
    private int                 periodId;
    private String              name;
    private String              marketDescription;

    @JsonIgnore
    private OffsetDateTime      createdAt;

    @JsonIgnore
    private OffsetDateTime      updatedAt;

    @JsonIgnore
    private String              eventId;

    private List<Participant>   participants;
}
