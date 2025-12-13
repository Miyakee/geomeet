package com.geomeet.api.application.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CreateSessionResultTest {

    @Test
    void shouldCreateResultSuccessfully() {
        // Given
        Long sessionId = 100L;
        String sessionIdString = "test-session-id";
        Long initiatorId = 1L;
        String status = "Active";
        String createdAt = "2024-01-01T00:00:00";

        // When
        CreateSessionResult result = CreateSessionResult.builder()
            .sessionId(sessionId)
            .sessionIdString(sessionIdString)
            .initiatorId(initiatorId)
            .status(status)
            .createdAt(createdAt)
            .build();

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.getSessionId());
        assertEquals(sessionIdString, result.getSessionIdString());
        assertEquals(initiatorId, result.getInitiatorId());
        assertEquals(status, result.getStatus());
        assertEquals(createdAt, result.getCreatedAt());
    }
}

