package com.bix.event_consumer.repositories;

import com.bix.event_consumer.models.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TeamRepository {
    private final JdbcTemplate jdbcTemplate;

    public void addTeam(Team team){
        log.info("TeamRepo::Attempting to insert  team {} ", team.getTeamId());

        // Safely extract conference data
        Integer conferenceId = null;
        String leagueName = null;

        if(team.getConference() != null){
            conferenceId = team.getConference().getConferenceId();
            leagueName = team.getConference().getName();
        } else if(team.getConferenceId() != null && team.getConferenceId() != 0) {
            conferenceId = team.getConferenceId();
        }


        String sql = """
                INSERT INTO teams(
                    team_id,
                    event_id,
                    name,
                    mascot,
                    abbreviation,
                    is_home,
                    is_away,
                    record,
                    conference_id,
                    division_id,
                    ranking,
                    league_name,
                    created_at,
                    updated_at
                ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    name                = VALUES(name),
                    mascot              = VALUES(mascot),
                    abbreviation        = VALUES(abbreviation),
                    is_home             = VALUES(is_home),
                    is_away             = VALUES(is_away),
                    record              = VALUES(record),
                    conference_id       = VALUES(conference_id),
                    division_id         = VALUES(division_id),
                    ranking             = VALUES(ranking),
                    league_name         = VALUES(league_name),
                    updated_at          = NOW()
                """;
        this.jdbcTemplate.update(
                sql,
                team.getTeamId(),
                team.getEventId(),
                team.getName(),
                team.getMascot(),
                team.getAbbreviation(),
                team.getIsHome(),
                team.getIsAway(),
                team.getRecord(),
                conferenceId,
                team.getDivisionId(),
                team.getRanking(),
                leagueName

        );

        log.info(
                "TeamRepository::team {} for event {} inserted successfully",
                team.getTeamId(),
                team.getEventId()
                );
    }
}
