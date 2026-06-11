package com.bix.event_consumer.repositories;

import com.bix.event_consumer.events.BetStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionsRepository {
    private final JdbcTemplate jdbcTemplate;

    public void addTransaction(BetStatusUpdate bet, String createdBy) {
        String query = """
                INSERT INTO transactions
                    (profile_id,reference,type,amount,status,created_by,created_at)
                VALUES (?,?,?,?,?,?,?)
                """;

        int rowAffected = this.jdbcTemplate.update(
                query,
                bet.getProfileId(),
                bet.getReference(),
                bet.getType(),
                bet.getAmount(),
                bet.getCurrentStatus(),
                createdBy,
                bet.getUpdateAt()
        );

        if(rowAffected < 1){
            log.warn(
                    "Could not add the transaction for betId={} and profileId={}",
                    bet.getBetId(),
                    bet.getProfileId()
            );
        }
    }
}
