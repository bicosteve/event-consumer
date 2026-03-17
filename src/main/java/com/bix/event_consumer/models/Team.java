package com.bix.event_consumer.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private Integer id;

    private Integer teamId;

    @JsonIgnore
    private String eventId;

    private String name;
    private String mascot;
    private String abbreviation;
    private Integer conferenceId;
    private Integer divisionId;
    private Integer ranking;
    private String record;
    private Boolean isAway;
    private Boolean isHome;
    private Conference conference;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;
}
