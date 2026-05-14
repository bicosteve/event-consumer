package com.bix.event_consumer.repositories;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Score;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ScoreRepository {
    private final JdbcTemplate jdbcTemplate;

    // 01. Add score to scores table
    public void addScores(Score score){
        log.info("Attempting to add score for event {}", score.getEventId());
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

        log.info("Inserted scores for event {} ", score.getEventId());
    }

    // 02. Find Event
    public Score findScoreByEventId(String eventId){
        log.info("Fetching score for event {} ", eventId);
        String query = """
                SELECT
                    id,
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
                FROM scores
                WHERE event_id = ?
                """;
        List<Score> scores = this.jdbcTemplate.query(
                query,
                (rs,rowNum) -> Score.builder()
                        .scoreId(rs.getLong("id"))
                        .eventId(rs.getString("event_id"))
                        .eventStatus(EventStatus.fromCode(rs.getInt("event_status")))
                        .eventStatusDetail(rs.getString("event_status_detail"))
                        .teamIdAway(rs.getInt("team_id_away"))
                        .teamIdHome(rs.getInt("team_id_home"))
                        .winnerAway(rs.getInt("winner_away"))
                        .winnerHome(rs.getInt("winner_home"))
                        .scoreAway(rs.getInt("score_away"))
                        .scoreHome(rs.getInt("score_home"))
                        .gameClock(rs.getInt("game_clock"))
                        .gamePeriod(rs.getInt("game_period"))
                        .broadcast(rs.getString("broadcast"))
                        .venueName(rs.getString("venue_name"))
                        .venueLocation(rs.getString("venue_location"))
                        .createdAt(rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC))
                        .updatedAt(rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC))
                        .build(),
                eventId);
        
        return scores.isEmpty() ? null : scores.get(0);
    }


}
