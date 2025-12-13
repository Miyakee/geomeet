-- Flyway migration script: Create sessions table
-- Version: 2

CREATE TABLE IF NOT EXISTS SESSIONS (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    initiator_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_sessions_initiator FOREIGN KEY (initiator_id) REFERENCES USERS(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_sessions_session_id ON SESSIONS(session_id);
CREATE INDEX IF NOT EXISTS idx_sessions_initiator_id ON SESSIONS(initiator_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON SESSIONS(status);

-- Create trigger to automatically update updated_at on row update
CREATE TRIGGER update_sessions_updated_at
    BEFORE UPDATE ON SESSIONS
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

