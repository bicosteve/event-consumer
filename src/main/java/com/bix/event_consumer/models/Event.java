package com.bix.event_consumer.models;

import com.bix.event_consumer.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    // This should be the events table
    @JsonIgnore
    private Integer id;

    private String eventId;
    private String eventUuid;
    private Integer sportId;
    private LocalDateTime eventDate;

    private Score score;
    private List<Team> teams;
    private Schedule schedule;
    private List<Market> markets;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;

    @JsonIgnore // do not deserialize
    private EventStatus status;
}
