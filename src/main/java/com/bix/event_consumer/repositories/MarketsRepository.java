package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MarketsRepository {
    private final JdbcTemplate jdbcTemplate;
    private Long generatedId;

    public Long addMarket(Market market){
        log.info(
                "MarketsRepository::attempt to add market {} ",
                market.getMarketDescription()
        );

        String sql = """
                INSERT INTO markets(
                    market_rundown_id,
                    market_type_id,
                    period_id,
                    name,
                    description,
                    event_id,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    market_rundown_id       = VALUES(market_rundown_id),
                    market_type_id          = VALUES(market_type_id),
                    period_id               = VALUES(period_id),
                    name                    = VALUES(name),
                    description             = VALUES(description),
                    updated_at              = NOW()
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update( connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                           sql,
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, market.getId());
                    ps.setInt(2,market.getMarketId());
                    ps.setInt(3, market.getPeriodId());
                    ps.setString(4,market.getName());
                    ps.setString(5,market.getMarketDescription());
                    ps.setString(6,market.getEventId());

                    return ps;

                }, keyHolder);

        // Get the generated market_id
        if(keyHolder.getKey() != null && keyHolder.getKey().longValue() > 0){
            // On fresh insert, we will get market_id from the KeyHolder
            this.generatedId = keyHolder.getKey().longValue();
        } else {
            // On DUPLICATE KEY UPDATE, we query for existing id
            // this is done since on duplicate, the KeyHolder returns 0 as market_id
            this.generatedId = this.queryForMarketId(market.getId(), market.getEventId());
        }

        log.info("MarketRepo::market {} inserted with id {}", market.getId(),this.generatedId);

        return this.generatedId;
    }

    private Long queryForMarketId(Integer marketRundownId, String eventId){
        String q = "SELECT id FROM markets WHERE market_rundown_id = ? ANd event_id ?";
        return this.jdbcTemplate.queryForObject(q, Long.class, marketRundownId, eventId);
    }

}
