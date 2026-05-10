package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Slip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BetSlipRepository{
    private final JdbcTemplate jdbcTemplate;

    // 01. Find all event's pending slips
    public List<Slip> findEventsPendingSlips(String eventId){
        log.info("BetSlipRepository::fetch pending slips for event {} ", eventId);

        String query = """
                SELECT
                    bet_slip_id,
                    bet_id,
                    event_id,
                    sport_id,
                    team_id,
                    market_id,
                    market_name,
                    participant_name,
                    odds,
                    special_bet_value,
                    status,
                    created_at,
                    updated_at
                FROM bet_slips
                WHERE event_id = ?
                AND status = 1
                """;

        List<Slip> slips = this.jdbcTemplate.query(
                query,
                (rs, rowNum) -> Slip.builder()
                        .betSlipId(rs.getLong("bet_slip_id"))
                        .betId(rs.getLong("bet_id"))
                        .eventId(rs.getString("event_id"))
                        .sportId(rs.getInt("sport_id"))
                        .teamId(rs.getInt("team_id"))
                        .marketId(rs.getInt("market_id"))
                        .marketName(rs.getString("market_name"))
                        .participantName(rs.getString("participant_name"))
                        .odds(rs.getBigDecimal("odds"))
                        .specialBetValue(rs.getString("special_bet_value"))
                        .status(rs.getInt("status"))
                        .createdAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC))
                        .updatedAt(rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC))
                        .build(),
                eventId);

        return slips;
    }

    // 02. Find all Bet's slips
    public List<Slip> findBetsSlip(Long betId){
        log.info("Fetching all slips for {} bet ",betId);
        String query = """
                SELECT
                    bet_slip_id,
                    bet_id,
                    event_id,
                    sport_id,
                    team_id,
                    market_id,
                    market_name,
                    participant_name,
                    odds,
                    special_bet_value,
                    status,
                    created_at,
                    updated_at
                FROM bet_slips
                WHERE bet_id = ?
                """;

        List<Slip> slips = this.jdbcTemplate.query(
                query,
                (rs,rowNum) -> Slip.builder()
                        .betSlipId(rs.getLong("bet_slip_id"))
                        .betId(rs.getLong("bet_id"))
                        .eventId(rs.getString("event_id"))
                        .sportId(rs.getInt("sport_id"))
                        .teamId(rs.getInt("team_id"))
                        .marketId(rs.getInt("market_id"))
                        .marketName(rs.getString("market_name"))
                        .participantName(rs.getString("participant_name"))
                        .odds(rs.getBigDecimal("odds"))
                        .specialBetValue(rs.getString("special_bet_value"))
                        .status(rs.getInt("status"))
                        .createdAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC))
                        .updatedAt(rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC))
                        .build(),
                betId
        );

        return slips;
    }

    // 03. Update Slip status
    public void updateSlipStatus(Long betSlipId, int status){
        log.info("Updating slip {} to status {} ", betSlipId,status);

        String query = """
                UPDATE bet_slips
                SET status = ?
                WHERE bet_slip_id = ?
                """;
        this.jdbcTemplate.update(query,status,betSlipId);
    }
}
