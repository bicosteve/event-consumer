package com.bix.event_consumer.cache;

import com.bix.event_consumer.enums.EventStatus;
import com.bix.event_consumer.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventCacheProjectionTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void v1ProjectionExactlyMatchesTheCheckedInGatewayContractFixture() throws Exception {
        Price price = Price.builder().priceId(41).id("price-provider").price(-110).isMainLine(true)
                .participantId(31L).bookMarkerId(9).handicapValue("-1.5").lineId("line-1")
                .closedAt(OffsetDateTime.parse("2030-01-02T03:05:00Z")).build();
        Line line = Line.builder().id("line-1").value("-1.5").prices(new LinkedHashMap<>()).build();
        line.getPrices().put("9", price);
        Event event = Event.builder().id(1).eventId("event-provider").eventUuid("event-uuid").sportId(7)
                .eventDate(OffsetDateTime.parse("2030-01-02T03:04:05Z"))
                .schedule(Schedule.builder().seasonType("regular").seasonYear(2030).eventName("Lions v Tigers").eventHeadline("Tonight").build())
                .teams(List.of(Team.builder().id(10).teamId(20).eventId("event-provider").name("Lions").abbreviation("LIO").isHome(true).isAway(false)
                        .conference(Conference.builder().name("Premier").build()).build()))
.markets(List.of(
Market.builder().localId(20L).id(21L).marketId(22).periodId(1).name("Spread").marketDescription("Main").eventId("event-provider")
.participants(List.of(Participant.builder().participantId(31).id(32).type("team").name("Lions").marketId(21L).lines(List.of(line)).build())).build(),
Market.builder().localId(23L).id(24L).marketId(25).periodId(0).eventId("event-provider").build()))
                .score(Score.builder().scoreId(44L).eventId("event-provider").eventStatus(EventStatus.STATUS_IN_PROGRESS).eventStatusDetail("Q1")
                        .teamIdAway(20).teamIdHome(21).winnerAway(0).winnerHome(1).scoreAway(1).scoreHome(2).gameClock(120).gamePeriod(1).build())
                .build();

        JsonNode actual = mapper.readTree(mapper.writeValueAsString(EventCacheProjection.from(event)));
        try (InputStream fixture = getClass().getResourceAsStream("/event-cache-v1.json")) {
            assertNotNull(fixture, "event-cache-v1 JSON contract fixture must be checked in");
            assertEquals(mapper.readTree(fixture), actual);
        }
    }

 @Test
 void participantMarketIdUsesItsParentProviderMarketIdRatherThanTheLocalMarketForeignKey() throws Exception {
  Event event = Event.builder().eventId("event-provider")
   .markets(List.of(Market.builder().localId(9001L).id(21L).marketId(22).periodId(1)
    .participants(List.of(Participant.builder().participantId(31).id(32).type("team").name("Lions")
     .marketId(9001L).lines(List.of()).build()))
    .build()))
   .build();

  JsonNode participant = mapper.readTree(mapper.writeValueAsString(EventCacheProjection.from(event)))
   .path("markets").get(0).path("participants").get(0);

  assertEquals(21L, participant.path("market_id").longValue());
 }
}
