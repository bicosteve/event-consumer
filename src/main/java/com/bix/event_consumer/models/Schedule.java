package com.bix.event_consumer.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    private Boolean conferenceCompetition;
    private String seasonType;
    private Integer seasonYear;
    private String eventName;
    private String eventHeadline;
    private String attendance;
}
