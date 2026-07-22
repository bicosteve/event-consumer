package com.bix.event_consumer.cache;

import com.bix.event_consumer.models.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EventCacheProjection(
        @JsonProperty("schema_version") int schemaVersion,
        Integer id,
        @JsonProperty("event_id") String eventId,
        @JsonProperty("event_uuid") String eventUuid,
        @JsonProperty("sport_id") Integer sportId,
        @JsonProperty("event_date") OffsetDateTime eventDate,
        @JsonProperty("season_type") String seasonType,
        @JsonProperty("season_year") Integer seasonYear,
        @JsonProperty("event_name") String eventName,
        @JsonProperty("event_headline") String eventHeadline,
        @JsonProperty("event_status") Integer eventStatus,
        List<TeamProjection> teams,
        List<MarketProjection> markets,
        ScoreProjection score) {

    public static EventCacheProjection from(Event event) {
        Schedule schedule = event.getSchedule();
        return new EventCacheProjection(1, event.getId(), event.getEventId(), event.getEventUuid(), event.getSportId(),
                event.getEventDate(), schedule == null ? null : schedule.getSeasonType(),
                schedule == null ? null : schedule.getSeasonYear(), schedule == null ? null : schedule.getEventName(),
                schedule == null ? null : schedule.getEventHeadline(), status(event.getScore()),
                event.getTeams() == null ? List.of() : event.getTeams().stream().map(TeamProjection::from).toList(),
                event.getMarkets() == null ? List.of() : event.getMarkets().stream().map(MarketProjection::from).toList(),
                ScoreProjection.from(event.getScore()));
    }

    private static Integer status(Score score) {
        return score == null || score.getEventStatus() == null ? null : score.getEventStatus().getCode();
    }

    record TeamProjection(Integer id, @JsonProperty("team_id") Integer teamId,
                          @JsonProperty("event_id") String eventId, String name, String abbreviation,
                          @JsonProperty("is_home") Boolean isHome, @JsonProperty("is_away") Boolean isAway,
                          @JsonProperty("league_name") String leagueName) {
        static TeamProjection from(Team team) {
            return new TeamProjection(team.getId(), team.getTeamId(), team.getEventId(), team.getName(),
                    team.getAbbreviation(), team.getIsHome(), team.getIsAway(),
                    team.getConference() == null ? null : team.getConference().getName());
        }
    }

    record MarketProjection(@JsonProperty("local_id") Long localId,
                            @JsonProperty("market_id") Long marketId,
                            @JsonProperty("market_type_id") Integer marketTypeId,
                            @JsonProperty("period_id") int periodId, String name,
                            @JsonProperty("market_description") String marketDescription,
                            @JsonProperty("event_id") String eventId, List<ParticipantProjection> participants) {


    static MarketProjection from(Market market) {
            return new MarketProjection(market.getLocalId(), market.getId(), market.getMarketId(), market.getPeriodId(),
            market.getName(), market.getMarketDescription(), market.getEventId(),
            market.getParticipants() == null ? List.of()
            : market.getParticipants().stream().map(participant -> ParticipantProjection.from(participant, market.getId())).toList());
    }

 }

    record ParticipantProjection(
            @JsonProperty("participant_id") Integer participantId, Integer id, String type, String name,
            @JsonProperty("market_id") Long marketId,

            List<LineProjection> lines) {
                 static ParticipantProjection from(Participant participant, Long providerMarketId) {
                     return new ParticipantProjection(participant.getParticipantId(), participant.getId(), participant.getType(),
                     participant.getName(), providerMarketId, participant.getLines() == null ? List.of()
                     : participant.getLines().stream().map(LineProjection::from).toList());
                 }
            }

    record LineProjection(String id, String value, Map<String, PriceProjection> prices) {
        static LineProjection from(Line line) {
            Map<String, PriceProjection> prices = new LinkedHashMap<>();
            if (line.getPrices() != null) {
                line.getPrices().forEach((key, value) -> prices.put(key, PriceProjection.from(value)));
            }
            return new LineProjection(line.getId(), line.getValue(), prices);
        }
    }

    record PriceProjection(@JsonProperty("price_id") Integer priceId,
                           @JsonProperty("rundown_id") String rundownId, int price,
                           @JsonProperty("is_main_line") boolean isMainLine, BigDecimal odds,
                           @JsonProperty("participant_id") Long participantId,
                           @JsonProperty("handicap_value") String handicapValue,
                           @JsonProperty("line_id") String lineId,
                           @JsonProperty("closed_at") OffsetDateTime closedAt) {
        static PriceProjection from(Price price) {
            return new PriceProjection(price.getPriceId(), price.getId(), price.getPrice(), price.isMainLine(),
                    price.getOdds(), price.getParticipantId(), price.getHandicapValue(), price.getLineId(),
                    price.getClosedAt());
        }
    }

    record ScoreProjection(
            @JsonProperty("score_id") Long scoreId,
            @JsonProperty("event_id") String eventId,
            @JsonProperty("event_status") Integer eventStatus,
            @JsonProperty("event_status_detail") String eventStatusDetail,
            @JsonProperty("team_id_away") Integer teamIdAway,
            @JsonProperty("team_id_home") Integer teamIdHome,
            @JsonProperty("winner_away") int winnerAway,
            @JsonProperty("winner_home") int winnerHome,
            @JsonProperty("score_away") int scoreAway,
            @JsonProperty("score_home") int scoreHome,
            @JsonProperty("game_clock") int gameClock,
            @JsonProperty("game_period") int gamePeriod) {

            static ScoreProjection from(Score score) {
                return score == null ? null : new ScoreProjection(score.getScoreId(), score.getEventId(), status(score),
                        score.getEventStatusDetail(), score.getTeamIdAway(), score.getTeamIdHome(), score.getWinnerAway(),
                        score.getWinnerHome(), score.getScoreAway(), score.getScoreHome(), score.getGameClock(),
                        score.getGamePeriod());
            }
    }
}
