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
@Builder
public class Market {
// This should be the market table
private Long localId;
private Long id;
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

private List<Participant> participants;

public Market(Long id, Integer marketId, int periodId, String name, String marketDescription,
              OffsetDateTime createdAt, OffsetDateTime updatedAt, String eventId,
              List<Participant> participants) {
this(null, id, marketId, periodId, name, marketDescription, createdAt, updatedAt, eventId, participants);
}

public Market(Long localId, Long id, Integer marketId, int periodId, String name,
              String marketDescription, OffsetDateTime createdAt, OffsetDateTime updatedAt,
              String eventId, List<Participant> participants) {
this.localId = localId;
this.id = id;
this.marketId = marketId;
this.periodId = periodId;
this.name = name;
this.marketDescription = marketDescription;
this.createdAt = createdAt;
this.updatedAt = updatedAt;
this.eventId = eventId;
this.participants = participants;
}
}
