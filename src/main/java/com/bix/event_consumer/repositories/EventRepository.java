package com.bix.event_consumer.repositories;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.Event;
import com.bix.event_consumer.models.Market;
import com.bix.event_consumer.models.Participant;
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
    private final PriceRepository priceRepository;


    // 01. Insert event
    private void insertEvent(Event event){
        log.info(
                "EventRepository::Attempt to insert event with details {}",
                event.getEventId()
        );

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
                    updated_at      = NOW()
                """;


        // 03. Insert then event
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

    }


    // 02. Insert Teams
    private void insertTeams(Event event){
        if(event.getTeams() != null){
            event.getTeams().forEach(team ->{
                team.setEventId(event.getEventId());
                this.teamRepository.addTeam(team);
            });
        } else {
            log.warn("EventRepository::no teams for event {} ", event.getEventId());
        }
    }

    // 03. Insert Markets
    private void insertMarkets(Event event){
        if(event.getMarkets() != null){
            event.getMarkets().forEach(market -> {
                market.setEventId(event.getEventId());

                Long marketId = this.marketsRepository.addMarket(market);

                this.insertParticipants(market,marketId);
            });
        } else {
            log.warn("EventRepository::no markets for event {}", event.getEventId());
        }
    }

    // 04. Insert Scores
    private void insertScore(Event event){
        if(event.getScore() != null){
            this.scoreRepository.addScores(event.getScore());
        }else {
            log.warn("EventRepository::no score for event {}", event.getEventId());
        }
    }

    // 05. Insert Prices/Odds
    private void insertPrices(Participant participant, Long participantId){
        if(participant.getLines() != null){
            participant.getLines().forEach(line -> {
                // key = 23 which is the  bookmakerId
                // value = Price(...) the actual price object

                line.getPrices().forEach((bookmakerId, price) ->{
                    price.setParticipantId(participantId);
                    price.setBookMarkerId(Integer.parseInt(bookmakerId)); // "23" -> 23
                    price.setLineId(line.getId());
                    price.setHandicapValue(line.getValue());

                    this.priceRepository.addPrice(price);
                });
            });
        } else {
            log.warn("EventRepository::no lines for participant {}", participant.getId());
        }
    }

    // 06. Insert Participants
    private void insertParticipants(Market market, Long marketId){
        if(market.getParticipants() != null){
            market.getParticipants().forEach(participant -> {
                participant.setMarketId(marketId);
                Long participantId = this.participantRepository.addParticipant(participant);
                this.insertPrices(participant,participantId);
            });
        }else{
            log.warn("EventRepository::no participants for market {}", marketId);
        }
    }


    @Transactional
    public void updateEvent(Event event){
        log.info("EventRepository::Attempt to insert event - {} ", event.getEventId());

        this.insertEvent(event);
        this.insertScore(event);
        this.insertTeams(event);
        this.insertMarkets(event);

        log.info("EventRepository::Event {} inserted", event.getEventId());

    }
}
