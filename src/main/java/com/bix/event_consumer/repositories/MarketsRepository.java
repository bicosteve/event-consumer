package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;


@Repository
@RequiredArgsConstructor
@Slf4j
public class MarketsRepository {
    private final JdbcTemplate jdbcTemplate;

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
                ) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW()) as new_market
                ON DUPLICATE KEY UPDATE
                    market_type_id          = new_market.market_type_id,
                    period_id               = new_market.period_id,
                    name                    = new_market.name,
                    description             = new_market.description,
                    event_id                = new_market.event_id,
                    updated_at              = NOW()
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = this.jdbcTemplate.update( connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql,new String[]{"id"});


                    ps.setInt(1, market.getId());
                    ps.setInt(2,market.getMarketId());
                    ps.setInt(3, market.getPeriodId());
                    ps.setString(4,market.getName());
                    ps.setString(5,market.getMarketDescription());
                    ps.setString(6,market.getEventId());

                    return ps;

                }, keyHolder);



        // Get the generated market_id
        Long generatedId;

        // MySQL behavior for ON DUPLICATE KEY UPDATE;
        // returns 1 when new row is inserted.
        // returns 2 when an existing row is updated.
        // returns 0 when an existing row is found but there is no update on the data.

        if(rowsAffected == 1 && keyHolder.getKey() != null){
            // On fresh insert,
            // we will get market_id from the KeyHolder
            generatedId = keyHolder.getKey().longValue();
        } else {
            // On DUPLICATE KEY UPDATE i.e 2 or 0, we query for existing id
            // Fallback to querying the ID manually
            // this is done since on duplicate, the KeyHolder returns 0 as market_id
            generatedId = this.queryForMarketId(market.getId(), market.getEventId());
        }

        log.info("MarketRepo::market {} inserted with id {}", market.getId(),generatedId);

        return generatedId;
    }

    private Long queryForMarketId(Integer marketRundownId, String eventId){
        String q = "SELECT id FROM markets WHERE market_rundown_id = ? AND event_id = ?";
        return this.jdbcTemplate.queryForObject(q, Long.class, marketRundownId, eventId);
    }

}
