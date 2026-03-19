package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Score;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ScoreRepository {
    private final JdbcTemplate jdbcTemplate;

    public void addScores(Score score){
        log.info("ScoreRepository::attempting to insert Score for event {}", score.getEventId());
        String sql = """
                INSERT INTO scores(
                    event_id,
                    event_status,
                    event_status_detail,
                    team_id_away,
                    team_id_home,
                    winner_away,
                    winner_home,
                    score_away,
                    score_home,
                    game_clock,
                    game_period,
                    broadcast,
                    venue_name,
                    venue_location,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    event_status            = VALUES(event_status),
                    event_status_detail     = VALUES(event_status_detail),
                    team_id_away            = VALUES(team_id_away),
                    team_id_home            = VALUES(team_id_home),
                    winner_away             = VALUES(winner_away),
                    winner_home             = VALUES(winner_home),
                    score_away              = VALUES(score_away),
                    score_home              = VALUES(score_home),
                    game_clock              = VALUES(game_clock),
                    game_period             = VALUES(game_period),
                    broadcast               = VALUES(broadcast),
                    venue_name              = VALUES(venue_name),
                    venue_location          = VALUES(venue_location),
                    updated_at              = NOW()
                """;

        this.jdbcTemplate.update(
                sql,
                score.getEventId(),
                score.getEventStatus() != null ? score.getEventStatus().getCode() : 0,
                score.getEventStatusDetail(),
                score.getTeamIdAway(),
                score.getTeamIdHome(),
                score.getWinnerAway(),
                score.getWinnerHome(),
                score.getScoreAway(),
                score.getScoreHome(),
                score.getGameClock(),
                score.getGamePeriod(),
                score.getBroadcast(),
                score.getVenueName(),
                score.getVenueLocation()
        );

        log.info("ScoreRepository::Inserted scores for event {} ", score.getEventId());
    }
}
