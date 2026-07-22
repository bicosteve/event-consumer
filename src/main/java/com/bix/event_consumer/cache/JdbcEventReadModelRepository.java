package com.bix.event_consumer.cache;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class JdbcEventReadModelRepository implements EventReadModelRepository {
    private final JdbcTemplate jdbc;

    @Override
    public Optional<Event> findByEventId(String eventId) {
        List<Event> events = jdbc.query("SELECT * FROM rundown_event WHERE event_id = ?", this::event, eventId);
        assemble(events);
        return events.stream().findFirst();
    }

    @Override
    public Optional<UpcomingCursor> findUpcomingWatermark(OffsetDateTime after) {
        List<UpcomingCursor> rows = jdbc.query("SELECT event_date, event_id FROM rundown_event WHERE event_date > ? ORDER BY event_date DESC, event_id DESC LIMIT 1",
                (rs, n) -> new UpcomingCursor(rs.getTimestamp("event_date").toInstant().atOffset(ZoneOffset.UTC), rs.getString("event_id")), Timestamp.from(after.toInstant()));
        return rows.stream().findFirst();
    }

    @Override
    public List<Event> findUpcoming(OffsetDateTime after, UpcomingCursor cursor, UpcomingCursor watermark, int limit) {
        if (limit <= 0) throw new IllegalArgumentException("batch size must be positive");
        String sql = "SELECT * FROM rundown_event WHERE event_date > ? AND (event_date < ? OR (event_date = ? AND event_id <= ?))"
                + (cursor == null ? "" : " AND (event_date > ? OR (event_date = ? AND event_id > ?))")
                + " ORDER BY event_date ASC, event_id ASC LIMIT ?";
        List<Object> args = new ArrayList<>(List.of(Timestamp.from(after.toInstant()), Timestamp.from(watermark.eventDate().toInstant()), Timestamp.from(watermark.eventDate().toInstant()), watermark.eventId()));
        if (cursor != null) args.addAll(List.of(Timestamp.from(cursor.eventDate().toInstant()), Timestamp.from(cursor.eventDate().toInstant()), cursor.eventId()));
        args.add(limit);
        List<Event> events = jdbc.query(sql, this::event, args.toArray());
        assemble(events);
        return events;
    }

    private Event event(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        return Event.builder().id(rs.getInt("id")).eventId(rs.getString("event_id")).eventUuid(rs.getString("event_uuid")).sportId(rs.getInt("sport_id"))
                .eventDate(rs.getTimestamp("event_date").toInstant().atOffset(ZoneOffset.UTC))
                .schedule(Schedule.builder().seasonType(rs.getString("season_type")).seasonYear((Integer) rs.getObject("season_year")).eventName(rs.getString("event_name")).eventHeadline(rs.getString("event_headline")).build()).build();
    }

    private void assemble(List<Event> events) {
        if (events.isEmpty()) return;
        List<String> ids = events.stream().map(Event::getEventId).toList();
        String in = String.join(",", Collections.nCopies(ids.size(), "?"));
        Map<String, Event> roots = new LinkedHashMap<>(); events.forEach(e -> { e.setTeams(new ArrayList<>()); e.setMarkets(new ArrayList<>()); roots.put(e.getEventId(), e); });
        jdbc.query("SELECT * FROM teams WHERE event_id IN (" + in + ") ORDER BY event_id, team_id", (RowCallbackHandler) rs -> roots.get(rs.getString("event_id")).getTeams().add(Team.builder().id(rs.getInt("id")).teamId(rs.getInt("team_id")).eventId(rs.getString("event_id")).name(rs.getString("name")).abbreviation(rs.getString("abbreviation")).isHome(rs.getBoolean("is_home")).isAway(rs.getBoolean("is_away")).conference(Conference.builder().conferenceId((Integer)rs.getObject("conference_id")).name(rs.getString("league_name")).build()).build()), ids.toArray());
        jdbc.query("SELECT * FROM scores WHERE event_id IN (" + in + ")", (RowCallbackHandler) rs -> roots.get(rs.getString("event_id")).setScore(Score.builder().scoreId(rs.getLong("id")).eventId(rs.getString("event_id")).eventStatus(EventStatus.fromCode(rs.getInt("event_status"))).eventStatusDetail(rs.getString("event_status_detail")).teamIdAway((Integer)rs.getObject("team_id_away")).teamIdHome((Integer)rs.getObject("team_id_home")).winnerAway(rs.getInt("winner_away")).winnerHome(rs.getInt("winner_home")).scoreAway(rs.getInt("score_away")).scoreHome(rs.getInt("score_home")).gameClock(rs.getInt("game_clock")).gamePeriod(rs.getInt("game_period")).build()), ids.toArray());
        Map<Long, Market> markets = new LinkedHashMap<>();
        jdbc.query("SELECT * FROM markets WHERE event_id IN (" + in + ") ORDER BY event_id, id", (RowCallbackHandler) rs -> { Market m=Market.builder().localId(rs.getLong("id")).id(rs.getLong("market_rundown_id")).marketId(rs.getInt("market_type_id")).periodId(rs.getInt("period_id")).name(rs.getString("name")).marketDescription(rs.getString("description")).eventId(rs.getString("event_id")).participants(new ArrayList<>()).build(); roots.get(m.getEventId()).getMarkets().add(m); markets.put(m.getLocalId(),m); }, ids.toArray());
        if (markets.isEmpty()) return;
        String marketIn = String.join(",", Collections.nCopies(markets.size(), "?")); Map<Integer, Participant> participants=new LinkedHashMap<>();
        jdbc.query("SELECT * FROM participants WHERE market_id IN (" + marketIn + ") ORDER BY market_id, participant_id", (RowCallbackHandler) rs -> { Participant p=Participant.builder().participantId(rs.getInt("participant_id")).id(Integer.valueOf(rs.getString("rundown_id"))).type(rs.getString("type")).name(rs.getString("name")).marketId(rs.getLong("market_id")).lines(new ArrayList<>()).build(); markets.get(p.getMarketId()).getParticipants().add(p); participants.put(p.getParticipantId(),p); }, markets.keySet().toArray());
        if (participants.isEmpty()) return;
        String participantIn=String.join(",", Collections.nCopies(participants.size(), "?")); Map<Integer, Map<String, Line>> lines=new HashMap<>();
        jdbc.query("SELECT * FROM prices WHERE participant_id IN (" + participantIn + ") ORDER BY participant_id, line_id, bookmaker_id", (RowCallbackHandler) rs -> { int id=rs.getInt("participant_id"); Map<String,Line> ps=lines.computeIfAbsent(id,k->new LinkedHashMap<>()); String lineId=rs.getString("line_id"); String handicap=rs.getString("handicap_value"); Line line=ps.computeIfAbsent(lineId,k->Line.builder().id(lineId).value(handicap).prices(new LinkedHashMap<>()).build()); line.getPrices().put(String.valueOf(rs.getInt("bookmaker_id")),Price.builder().priceId(rs.getInt("price_id")).id(rs.getString("rundown_id")).price(rs.getInt("price")).isMainLine(rs.getBoolean("is_main_line")).participantId((long)id).bookMarkerId(rs.getInt("bookmaker_id")).handicapValue(rs.getString("handicap_value")).lineId(lineId).closedAt(rs.getTimestamp("closed_at") == null ? null : rs.getTimestamp("closed_at").toInstant().atOffset(ZoneOffset.UTC)).build()); }, participants.keySet().toArray());
        participants.forEach((id,p)->p.setLines(new ArrayList<>(lines.getOrDefault(id,Map.of()).values())));
    }
}
