package com.bix.event_consumer.models;

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
    private Integer id;
    private String eventId;
    private String eventUuid;
    private Integer sportId;
    private LocalDateTime eventDate;

    private Score score;
    private List<Team> teams;
    private Schedule schedule;
    private List<Market> markets;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
