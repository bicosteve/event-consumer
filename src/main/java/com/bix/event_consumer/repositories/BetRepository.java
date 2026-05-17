package com.bix.event_consumer.repositories;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.models.Bet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BetRepository{
    private final JdbcTemplate jdbcTemplate;

    // 01. Update Bet Status
    @Transactional
    public BetStatusUpdate updateBetStatus(Long betId, Integer status){
        String query = "UPDATE bets SET status = ? WHERE bet_id = ?";
        try{
            log.info("Attempting to update bet={} from status 1 to status{} ", betId, status);

            int rowsAffected = this.jdbcTemplate.update(query,status,betId);
            if(rowsAffected == 0){
                throw new RuntimeException("Failed to update bet " + betId + " status to " + status);
            }

            return BetStatusUpdate.builder()
                    .betId(betId)
                    .previousStatus(1)
                    .currentStatus(status)
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
