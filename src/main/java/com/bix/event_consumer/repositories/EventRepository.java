package com.bix.event_consumer.repositories;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ScoreRepository scoreRepository;
    private final TeamRepository teamRepository;
    private final MarketsRepository marketsRepository;
    private final ParticipantRepository participantRepository;
    private Long marketId;

    @Transactional
    public void updateEvent(Event event){
        log.info("EventRepository::Attempt to insert event {}",event.getEventId());

        // Get the status from score before inserting
        EventStatus status = event.getScore() != null ? event.getScore().getEventStatus() : null;

        // 02. Get schedule fields safely
        Schedule schedule = event.getSchedule();
        String seasonType = schedule != null ? schedule.getSeasonType() : null;
        Integer seasonYear = schedule != null ? schedule.getSeasonYear() : null;
        String eventName = schedule != null ? schedule.getEventName() : null;
        String eventHeadline = schedule != null ? schedule.getEventHeadline() : null;

        String query = """
                INSERT INTO rundown_event(
                    event_id,
                    event_uuid,
                    sport_id,
                    event_date,
                    season_type,
                    season_year,
                    event_name,
                    event_headline,
                    event_status,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    event_uuid      = VALUES(event_uuid),
                    sport_id        = VALUES(sport_id),
                    event_date      = VALUES(event_date),
                    season_type     = VALUES(season_type),
                    season_year     = VALUES(season_year),
                    event_name      = VALUES(event_name),
                    event_headline  = VALUES(event_headline),
                    event_status    = VALUES(event_status),
                    update_at       = NOW()
                """;




        // 03. Insert event
        log.info("ConsumerRepo::event {}",event);
        this.jdbcTemplate.update(
                query,
                event.getEventId(),
                event.getEventUuid(),
                event.getSportId(),
                event.getEventDate(),
                seasonType,
                seasonYear,
                eventName,
                eventHeadline,
                status != null ? status.getCode() : 0
        );

        // 04. Insert Teams
        if(event.getTeams() != null){
            event.getTeams().forEach(team ->{
                team.setEventId(event.getEventId());
                this.teamRepository.addTeam(team);
            });
        }

        // 05. Insert the markets
        if(event.getMarkets() != null){
            event.getMarkets().forEach(market -> {
                market.setEventId(event.getEventId());
                this.marketId = this.marketsRepository.addMarket(market);
            });
        }

        // 06. Add Participant
        if(event.getMarkets() != null){
            event.getMarkets().forEach(market -> {
                if(market.getParticipants() != null){
                    market.getParticipants().forEach(participant -> {
                        participant.setMarketId(this.marketId.intValue());
                        this.participantRepository.addParticipant(participant);
                    });
                }
            });
        }


        // 07. Add Scores
        if(event.getScore() != null){
            this.scoreRepository.insertScore(event.getScore());
        }


        log.info("");

    }
}
