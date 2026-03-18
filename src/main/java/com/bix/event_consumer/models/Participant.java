package com.bix.event_consumer.models;

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
public class Participant {
    @JsonIgnore
    private Integer participantId;

    private Integer id;
    private String type;
    private String name;

    @JsonIgnore
    private Long marketId;

    private List<Line> lines;

    @JsonIgnore
    private  LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;
}
