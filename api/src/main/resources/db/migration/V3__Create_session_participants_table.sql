-- Flyway migration script: Create session participants table
-- Version: 3

CREATE TABLE IF NOT EXISTS SESSION_PARTICIPANTS (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_session_participants_session FOREIGN KEY (session_id) REFERENCES SESSIONS(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_participants_user FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE,
    CONSTRAINT uk_session_participants_session_user UNIQUE (session_id, user_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_session_participants_session_id ON SESSION_PARTICIPANTS(session_id);
CREATE INDEX IF NOT EXISTS idx_session_participants_user_id ON SESSION_PARTICIPANTS(user_id);

-- Create trigger to automatically update updated_at on row update
CREATE TRIGGER update_session_participants_updated_at
    BEFORE UPDATE ON SESSION_PARTICIPANTS
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

