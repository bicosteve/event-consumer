package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BetRepository{
    private final JdbcTemplate jdbcTemplate;

    // 01. Find Bet
    public Bet findById(Long betId){
        log.info("BetRepository::fetching bet {} ", betId);

        String query = """
                SELECT
                    bet_id,
                    profile_id,
                    stake,
                    is_bonus,
                    status,
                    total_odds,
                    possible_win,
                    created_at,
                    updated_at
                """;
        List<Bet> bets = this.jdbcTemplate.query(
                query,
                (rs, rowNum) -> Bet.builder()
                        .betId(rs.getLong("bet_id"))
                        .profileId(rs.getLong("profile_id"))
                        .stake(rs.getBigDecimal("stake"))
                        .isBonus(rs.getInt("is_bonus"))
                        .status(rs.getInt("status"))
                        .totalOdds(rs.getBigDecimal("total_odds"))
                        .possibleWin(rs.getBigDecimal("possible_win"))
                        .createdAt(rs.getObject("created_at", LocalDateTime.class))
                        .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
                        .build(),
                betId
        );

        return bets.isEmpty() ? null : bets.get(0);
    }

    // 02. Update Bet Status
    public void updateBetStatus(Long betId, int status){
        log.info("BetRepository::updating bet {} status to {} ", betId,status);

        String query = """
                UPDATE bets
                SET status = ?
                WHERE bet_id = ?
                """;

        this.jdbcTemplate.update("",status,betId);
    }

}
