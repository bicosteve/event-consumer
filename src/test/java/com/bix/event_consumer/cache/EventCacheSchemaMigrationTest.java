package com.bix.event_consumer.cache;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCacheSchemaMigrationTest {
    @Test
    void operationalMigrationGuardsTheExactMysql8CompositeIndexAndReportsIt() throws Exception {
        String sql = Files.readString(Path.of("db/migrations/20260722_add_rundown_event_date_id_index.sql"));

        assertTrue(sql.contains("information_schema.statistics"));
        assertTrue(sql.contains("table_schema = DATABASE()"));
        assertTrue(sql.contains("index_name = 'idx_rundown_event_date_id'"));
        assertTrue(sql.contains("ADD INDEX idx_rundown_event_date_id (event_date, event_id)"));
        assertTrue(sql.contains("CALL add_idx_rundown_event_date_id_if_absent()"));
        assertTrue(sql.contains("DROP PROCEDURE add_idx_rundown_event_date_id_if_absent"));
        assertTrue(Files.readString(Path.of("README.md")).contains("SHOW INDEX FROM rundown_event WHERE Key_name = 'idx_rundown_event_date_id'"));
    }
}
