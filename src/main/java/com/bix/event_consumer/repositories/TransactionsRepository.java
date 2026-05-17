package com.bix.event_consumer.repositories;


import com.bix.event_consumer.models.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionsRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Bet> rowMapper = (rs, rowNumber)->{
        Bet bet = new Bet();
        bet.setProfileId(rs.getLong("profile_id"));
        bet.setPossibleWin(rs.getBigDecimal("possible_win"));
        return bet;
    };

    private Bet getBetDetails(Long betId){
        String query = """
                SELECT profile_id,possible_win
                FROM bets
                WHERE bet_id = ?
                """;
        try{
            return this.jdbcTemplate.queryForObject(query, this.rowMapper, betId);
        }catch (EmptyResultDataAccessException e){
            throw new RuntimeException(
                    "Could not find bet with id %d because of %s "
                            .formatted(betId,e.getMessage()));
        }
    }

    public void addTransaction() {
        String query = """
                INSERT INTO transactions
                    (profile_id,reference,type,amount,status,created_by,created_at)
                VALUES (?,?,?,?,?,?,?)
                """;
        // For adding new transaction

    }
}
