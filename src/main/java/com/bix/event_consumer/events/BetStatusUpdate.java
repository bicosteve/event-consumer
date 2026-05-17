package com.bix.event_consumer.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetStatusUpdate {
    private Long betId;
    private Integer previousStatus;
    private Integer currentStatus;
    private LocalDateTime updateAt;
}
