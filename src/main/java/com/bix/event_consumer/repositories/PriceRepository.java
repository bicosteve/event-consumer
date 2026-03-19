package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PriceRepository {
    private final JdbcTemplate jdbcTemplate;

    public void addPrice(Price price){
        String sql = """
                INSERT INTO prices(
                    rundown_id,
                    price,
                    price_delta,
                    is_main_line,
                    odds,
                    participant_id,
                    bookmaker_id,
                    handicap_value,
                    line_id,
                    closed_at,
                    updated_at,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                ON DUPLICATE KEY UPDATE
                    price               = VALUES(price),
                    price_delta         = VALUES(price_delta),
                    is_main_line        = VALUES(is_main_line),
                    odds                = VALUES(odds),
                    participant_id      = VALUES(participant_id),
                    bookmaker_id        = VALUES(bookmaker_id),
                    handicap_value      = VALUES(handicap_value),
                    line_id             = VALUES(line_id),
                    closed_at           = VALUES(closed_at),
                    updated_at          = VALUES(updated_at)
                """;
        log.info(
                "PriceRepository::Attempting to add price {} - query {}",
                price.getPriceId(),
                sql
                );

        this.jdbcTemplate.update(
                sql,
                price.getId(),
                price.getPrice(),
                price.getPriceDelta(),
                price.isMainLine(),
                price.getOdds(),
                price.getParticipantId(),
                price.getBookMarkerId(),
                price.getHandicapValue(),
                price.getLineId(),
                price.getClosedAt(),
                price.getUpdatedAt()
        );

        log.info(
                "PriceRepository::price {} inserted successfully",
                price.getId()
        );

    }
}
