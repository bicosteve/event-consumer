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
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) AS new_score
                ON DUPLICATE KEY UPDATE
                    event_status            = new_score.event_status,
                    event_status_detail     = new_score.event_status_detail,
                    team_id_away            = new_score.team_id_away,
                    team_id_home            = new_score.team_id_home,
                    winner_away             = new_score.winner_away,
                    winner_home             = new_score.winner_home,
                    score_away              = new_score.score_away,
                    score_home              = new_score.score_home,
                    game_clock              = new_score.game_clock,
                    game_period             = new_score.game_period,
                    broadcast               = new_score.broadcast,
                    venue_name              = new_score.venue_name,
                    venue_location          = new_score.venue_location,
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
