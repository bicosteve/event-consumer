package com.bix.event_consumer.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Team {
    private Integer id;
    private Integer teamId;
    private String eventId;
    private String name;
    private String abbreviation;
    private Integer conferenceId;
    private Integer divisionId;
    private Integer ranking;
    private String record;
    private Boolean isAway;
    private Boolean isHome;
    private Conference conference;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
