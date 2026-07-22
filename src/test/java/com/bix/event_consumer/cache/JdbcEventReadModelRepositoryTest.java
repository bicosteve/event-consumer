package com.bix.event_consumer.cache;

import com.bix.event_consumer.models.Event;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcEventReadModelRepositoryTest {
    @Test
    void upcomingUsesKeysetBoundsAndBoundedAggregateQueries() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        OffsetDateTime after = OffsetDateTime.parse("2030-01-01T00:00:00Z");
        var cursor = new EventReadModelRepository.UpcomingCursor(after, "a");
        var watermark = new EventReadModelRepository.UpcomingCursor(OffsetDateTime.parse("2030-02-01T00:00:00Z"), "z");
        ResultSet eventRow = eventRow("b", "2030-01-02T00:00:00Z");
        when(jdbc.query(contains("FROM rundown_event"), any(RowMapper.class), any(Object[].class)))
                .thenAnswer(invocation -> List.of(((RowMapper<Event>) invocation.getArgument(1)).mapRow(eventRow, 0)));

        List<Event> result = new JdbcEventReadModelRepository(jdbc).findUpcoming(after, cursor, watermark, 10);

        assertEquals(List.of("b"), result.stream().map(Event::getEventId).toList());
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), any(RowMapper.class), any(Object[].class));
        verify(jdbc, atMost(6)).query(anyString(), any(RowCallbackHandler.class), any(Object[].class));
        String rootSql = sql.getValue();
        assertTrue(rootSql.contains("event_date > ? OR (event_date = ? AND event_id > ?)"));
        assertTrue(rootSql.contains("event_date < ? OR (event_date = ? AND event_id <= ?)"));
        assertFalse(rootSql.contains("OFFSET"));
    }

    @Test
    void rejectsNonPositiveBatchSize() {
        JdbcEventReadModelRepository repository = new JdbcEventReadModelRepository(mock(JdbcTemplate.class));
        var bound = new EventReadModelRepository.UpcomingCursor(OffsetDateTime.now(), "x");
        assertThrows(IllegalArgumentException.class,
                () -> repository.findUpcoming(OffsetDateTime.now(), null, bound, 0));
    }

    private ResultSet eventRow(String eventId, String date) throws Exception {
        ResultSet row = mock(ResultSet.class);
        when(row.getInt("id")).thenReturn(1);
        when(row.getString("event_id")).thenReturn(eventId);
        when(row.getString("event_uuid")).thenReturn("uuid-" + eventId);
        when(row.getInt("sport_id")).thenReturn(1);
        when(row.getTimestamp("event_date")).thenReturn(Timestamp.from(OffsetDateTime.parse(date).toInstant()));
        return row;
    }
}
