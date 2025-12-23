-- Flyway migration script: Add invite code to sessions table
-- Version: 6
-- Description: Add invite_code field to store a separate invitation code for joining sessions
-- This prevents users from joining sessions by guessing the session ID

ALTER TABLE SESSIONS
    ADD COLUMN IF NOT EXISTS invite_code VARCHAR(20) UNIQUE;

-- Create index for invite code lookups
CREATE INDEX IF NOT EXISTS idx_sessions_invite_code ON SESSIONS(invite_code);

-- Add comment to explain the field
COMMENT ON COLUMN SESSIONS.invite_code IS 'Invitation code required to join the session. Separate from session_id for security.';

-- Generate invite codes for existing sessions (backward compatibility)
-- Using a simple random string generator
UPDATE SESSIONS
SET invite_code = UPPER(SUBSTRING(MD5(RANDOM()::TEXT || session_id || id::TEXT), 1, 8))
WHERE invite_code IS NULL;

-- Make invite_code NOT NULL after populating existing rows
ALTER TABLE SESSIONS
    ALTER COLUMN invite_code SET NOT NULL;

