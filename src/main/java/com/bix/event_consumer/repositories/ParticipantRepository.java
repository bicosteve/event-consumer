package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Participant;
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
public class ParticipantRepository {
    private final JdbcTemplate jdbcTemplate;

    public Long addParticipant(Participant participant){
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
                    rundown_id      = VALUES(rundown_id),
                    type            = VALUES(type),
                    name            = VALUES(name),
                    updated_at      = NOW()
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, participant.getId());
            ps.setString(2, participant.getType());
            ps.setString(3,participant.getName());
            ps.setLong(4,participant.getMarketId());
            return ps;
        }, keyHolder);

        Long generatedId;
        if(keyHolder.getKey() != null && keyHolder.getKey().longValue() > 0){
            generatedId = keyHolder.getKey().longValue();
        } else {
            generatedId = this.queryParticipantId(participant.getId(),Long.valueOf(participant.getMarketId()));
        }

        log.info(
                "ParticipantRepo::Added participant {}",
                participant.getParticipantId()
        );

        return generatedId;
    }

    private Long queryParticipantId(Integer rundownId, Long marketId){
        String q = "SELECT participant_id FROM participants WHERE rundown_id = ? AND market_id = ?";
        return this.jdbcTemplate.queryForObject(q, Long.class, rundownId, marketId);
    }
}
