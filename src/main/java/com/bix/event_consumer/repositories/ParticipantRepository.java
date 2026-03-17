package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Participant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ParticipantRepository {
    private final JdbcTemplate jdbcTemplate;

    public void addParticipant(Participant participant){
        log.info(
                "ParticipantRepo::attempting to add participant {}",
                participant.getParticipantId()
        );

        String sql = """
                INSERT INTO participants(
                    rundown_id,
                    type,
                    name,
                    market_id,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    rundown_id      = VALUES(rundown_id)
                    type            = VALUES(type)
                    name            = VALUES(name)
                    updated_at      = NOW()
                """;


        this.jdbcTemplate.update(
                sql,
                participant.getMarketId(),
                participant.getType(),
                participant.getName(),
                participant.getMarketId()
        );

        log.info(
                "ParticipantRepo::Added participant {}",
                participant.getParticipantId()
        );

    }
}
