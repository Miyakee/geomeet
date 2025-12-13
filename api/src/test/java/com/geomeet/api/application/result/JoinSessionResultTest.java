package com.geomeet.api.application.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class JoinSessionResultTest {

    @Test
    void shouldCreateJoinSessionResultSuccessfully() {
        // Given
        Long participantId = 1L;
        Long sessionId = 100L;
        String sessionIdString = "test-session-id";
        Long userId = 2L;
        String joinedAt = "2024-01-01T00:00:00";
        String message = "Successfully joined the session";

        // When
        JoinSessionResult result = JoinSessionResult.builder()
            .participantId(participantId)
            .sessionId(sessionId)
            .sessionIdString(sessionIdString)
            .userId(userId)
            .joinedAt(joinedAt)
            .message(message)
            .build();

        // Then
        assertNotNull(result);
        assertEquals(participantId, result.getParticipantId());
        assertEquals(sessionId, result.getSessionId());
        assertEquals(sessionIdString, result.getSessionIdString());
        assertEquals(userId, result.getUserId());
        assertEquals(joinedAt, result.getJoinedAt());
        assertEquals(message, result.getMessage());
    }
}

