package com.bix.event_consumer.repositories;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.models.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BetRepository{
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Bet> betRowMapper = (rs, rowNumber)->{
        Bet bet = new Bet();
        bet.setBetId(rs.getLong("bet_id"));
        bet.setProfileId(rs.getLong("profile_id"));
        bet.setStake(rs.getBigDecimal("stake"));
        bet.setIsBonus(rs.getInt("is_bonus"));
        bet.setStatus(rs.getInt("status"));
        bet.setTotalOdds(rs.getBigDecimal("total_odds"));
        bet.setPossibleWin(rs.getBigDecimal("possible_win"));

        return bet;
    };

    public Bet getBetDetails(Long betId){
        String query = """
                SELECT
                    bet_id,
                    profile_id,
                    stake,
                    is_bonus,
                    status,
                    total_odds,
                    possible_win
                FROM bets
                WHERE bet_id = ?
                """;
        try{
            return this.jdbcTemplate.queryForObject(query, this.betRowMapper, betId);
        }catch (EmptyResultDataAccessException e){
            throw new RuntimeException(
                    "Could not find bet with id %d because of %s "
                            .formatted(betId,e.getMessage()));
        }
    }

    // 01. Update Bet Status
    public BetStatusUpdate updateBetStatus(Long betId, Integer status){

        // a. fetch the current bet before updating
        // we will get profile_id, possible_win, stake, current_status
        Bet bet = this.getBetDetails(betId);

        if(bet == null){
            log.error("Bet with id {} not found",betId);
            throw new RuntimeException("Bet with id " + betId + " not found");
        }

        // b. update the status of the bet
        String query = "UPDATE bets SET status = ? WHERE bet_id = ?";
        try{
            log.info(
                    "Attempting to update bet={} from status={} to status={}",
                    betId,
                    bet.getStatus(),
                    status);

            int rowsAffected = this.jdbcTemplate.update(query,status,betId);
            if(rowsAffected == 0){
                log.error("Failed to update betId={} to status={}",betId,status);
                throw new RuntimeException("Failed to update bet " + betId + " status to " + status);
            }

            log.info("Building betStatusUpdate using this bet details={}",bet);

            return BetStatusUpdate.builder()
                    .betId(betId)
                    .profileId(bet.getProfileId())
                    .amount(bet.getStake())
                    .possibleWin(bet.getPossibleWin())
                    .previousStatus(bet.getStatus())
                    .currentStatus(status)
                    .reference(UUID.randomUUID().toString())
                    .updateAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build();
        } catch(Exception e){
            log.error(
                    "Error updating bet={} to status={} because of={}",
                    betId,
                    status,
                    e.getMessage()
            );

            throw e;
        }
    }

}
