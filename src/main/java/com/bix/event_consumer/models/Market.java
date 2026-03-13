package com.bix.event_consumer.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Market {
    // This should be the market table
    private Integer id;
    private Integer marketId;
    private int periodId;
    private String name;
    private String marketDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String eventId;

    private List<Participant> participants;
}
