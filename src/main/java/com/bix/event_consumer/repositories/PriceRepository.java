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
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) AS new_price
                ON DUPLICATE KEY UPDATE
                    price               = new_price.price,
                    price_delta         = new_price.price_delta,
                    odds                = new_price.odds,
                    handicap_value      = new_price.handicap_value,
                    closed_at           = new_price.closed_at,
                    updated_at          = NOW()
                """;
        log.info(
                "PriceRepository::Attempting to add price for participant {} bookmaker {} line {}",
                price.getParticipantId(),
                price.getBookMarkerId(),
                price.getLineId()
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
                price.getClosedAt()
        );

        log.info(
                "PriceRepository::price for participant {} bookmaker {} line {} upserted successfully",
                price.getParticipantId(),
                price.getBookMarkerId(),
                price.getLineId()
        );

    }
}
