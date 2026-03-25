package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Participant;
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
public class ParticipantRepository {
    private final JdbcTemplate jdbcTemplate;

    public Long addParticipant(Participant participant){
        log.info(
                "ParticipantRepo::attempting to add participant {}",
                participant.getId()
        );

        String sql = """
                INSERT INTO participants(
                    rundown_id,
                    type,
                    name,
                    market_id,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, NOW(), NOW()) AS new_participant
                ON DUPLICATE KEY UPDATE
                    rundown_id      = new_participant.rundown_id,
                    type            = new_participant.type,
                    name            = new_participant.name,
                    updated_at      = NOW()
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"participant_id"});

            ps.setInt(1, participant.getId());
            ps.setString(2, participant.getType());
            ps.setString(3,participant.getName());
            ps.setLong(4,participant.getMarketId());

            return ps;

        }, keyHolder);

        Long generatedId;

        if(rowsAffected == 1 && keyHolder.getKey() != null){
            // On fresh insert,
            // we will get market_id from the KeyHolder
            generatedId = keyHolder.getKey().longValue();
        } else {
            // On DUPLICATE KEY UPDATE i.e 2 or 0, we query for existing id
            // Fallback to querying the ID manually
            // this is done since on duplicate, the KeyHolder returns 0 as market_id
            generatedId = this.queryParticipantId(participant.getId(),participant.getMarketId());
        }

        participant.setParticipantId(generatedId.intValue());

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
