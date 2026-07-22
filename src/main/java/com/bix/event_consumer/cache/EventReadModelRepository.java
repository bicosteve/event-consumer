package com.bix.event_consumer.cache;

import com.bix.event_consumer.models.Event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EventReadModelRepository {
    Optional<Event> findByEventId(String eventId);
    Optional<UpcomingCursor> findUpcomingWatermark(OffsetDateTime after);

    List<Event> findUpcoming(OffsetDateTime after, UpcomingCursor cursor, UpcomingCursor watermark, int limit);

    record UpcomingCursor(OffsetDateTime eventDate, String eventId) { }
}
