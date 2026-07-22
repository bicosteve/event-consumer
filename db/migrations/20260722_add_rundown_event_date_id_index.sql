-- MySQL 8 operational migration; safe to execute repeatedly.
-- Run against each existing production database before enabling cache reconciliation.
-- Example:
-- mysql --defaults-extra-file=/secure/path/event-consumer.cnf < db/migrations/20260722_add_rundown_event_date_id_index.sql

DELIMITER //

CREATE PROCEDURE add_idx_rundown_event_date_id_if_absent()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'rundown_event'
          AND index_name = 'idx_rundown_event_date_id'
    ) THEN
        ALTER TABLE rundown_event
            ADD INDEX idx_rundown_event_date_id (event_date, event_id);
    END IF;
END //

CALL add_idx_rundown_event_date_id_if_absent() //
DROP PROCEDURE add_idx_rundown_event_date_id_if_absent //

DELIMITER ;
