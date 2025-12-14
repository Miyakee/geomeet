-- Flyway migration script: Create participant locations table
-- Version: 4
-- This table stores location information for session participants.
-- Location is stored separately to allow frequent updates without affecting participant records.

CREATE TABLE IF NOT EXISTS PARTICIPANT_LOCATIONS (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_participant_locations_participant FOREIGN KEY (participant_id) REFERENCES SESSION_PARTICIPANTS(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_locations_session FOREIGN KEY (session_id) REFERENCES SESSIONS(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_locations_user FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_participant_locations_participant_id ON PARTICIPANT_LOCATIONS(participant_id);
CREATE INDEX IF NOT EXISTS idx_participant_locations_session_id ON PARTICIPANT_LOCATIONS(session_id);
CREATE INDEX IF NOT EXISTS idx_participant_locations_user_id ON PARTICIPANT_LOCATIONS(user_id);
CREATE INDEX IF NOT EXISTS idx_participant_locations_session_user ON PARTICIPANT_LOCATIONS(session_id, user_id);
CREATE INDEX IF NOT EXISTS idx_participant_locations_updated_at ON PARTICIPANT_LOCATIONS(updated_at DESC);

-- Unique constraint: one location record per participant
-- We'll update the existing record instead of creating new ones
CREATE UNIQUE INDEX IF NOT EXISTS uk_participant_locations_participant 
ON PARTICIPANT_LOCATIONS(participant_id);

-- Create trigger to automatically update updated_at on row update
CREATE TRIGGER update_participant_locations_updated_at
    BEFORE UPDATE ON PARTICIPANT_LOCATIONS
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

