-- Flyway migration script: Add meeting location to sessions table
-- Version: 5
-- Description: Add latitude and longitude fields to store the meeting location set by initiator

ALTER TABLE SESSIONS
    ADD COLUMN IF NOT EXISTS meeting_location_latitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS meeting_location_longitude DOUBLE PRECISION;

-- Add comment to explain the fields
COMMENT ON COLUMN SESSIONS.meeting_location_latitude IS 'Meeting location latitude set by session initiator';
COMMENT ON COLUMN SESSIONS.meeting_location_longitude IS 'Meeting location longitude set by session initiator';

