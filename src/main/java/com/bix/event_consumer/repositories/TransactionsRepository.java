package com.bix.event_consumer.repositories;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionsRepository {
    private final JdbcTemplate jdbcTemplate;

    public void addTransaction() {
        String query = """
                INSERT INTO transactions
                    (profile_id,reference,type,amount,status,created_by,created_at)
                VALUES (?,?,?,?,?,?,?)
                """;
        // For adding new transaction

    }
}
