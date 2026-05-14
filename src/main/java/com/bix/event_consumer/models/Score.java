package com.bix.event_consumer.models;

import com.bix.event_consumer.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Score {
    // This should be the outcome tables
    @JsonIgnore
    private Long            scoreId;

    private String          eventId;
    private EventStatus     eventStatus;
    private String          eventStatusDetail;
    private Integer         teamIdAway;
    private Integer         teamIdHome;
    private int             winnerAway;
    private int             winnerHome;
    private int             scoreAway;
    private int             scoreHome;
    private int             gameClock;
    private int             gamePeriod;
    private String          broadcast;
    private String          venueName;
    private String          venueLocation;

    @JsonIgnore
    private OffsetDateTime  createdAt;

    private OffsetDateTime  updatedAt;
}
