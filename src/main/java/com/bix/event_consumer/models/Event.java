package com.bix.event_consumer.models;

import com.bix.event_consumer.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    // This should be the events table
    @JsonIgnore
    private Integer id;

    private String          eventId;
    private String          eventUuid;
    private Integer         sportId;
    private OffsetDateTime  eventDate;

    private List<Team>      teams;
    private List<Market>    markets;
    private Score           score;
    private Schedule        schedule;

    @JsonIgnore
    private OffsetDateTime  createdAt;

    @JsonIgnore
    private OffsetDateTime  updatedAt;

    @JsonIgnore // do not deserialize
    private EventStatus     status;
}
