package com.geomeet.api.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SessionParticipantTest {

    @Test
    void shouldCreateSessionParticipantSuccessfully() {
        // Given
        Long sessionId = 100L;
        Long userId = 1L;

        // When
        SessionParticipant participant = SessionParticipant.create(sessionId, userId);

        // Then
        assertNotNull(participant);
        assertEquals(sessionId, participant.getSessionId());
        assertEquals(userId, participant.getUserId());
        assertNotNull(participant.getJoinedAt());
        assertNotNull(participant.getCreatedAt());
        assertNotNull(participant.getUpdatedAt());
    }

    @Test
    void shouldReconstructSessionParticipantFromPersistence() {
        // Given
        Long id = 1L;
        Long sessionId = 100L;
        Long userId = 1L;
        LocalDateTime joinedAt = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        String createdBy = "admin";
        String updatedBy = "admin";

        // When
        SessionParticipant participant = SessionParticipant.reconstruct(
            id,
            sessionId,
            userId,
            joinedAt,
            createdAt,
            updatedAt,
            createdBy,
            updatedBy
        );

        // Then
        assertNotNull(participant);
        assertEquals(id, participant.getId());
        assertEquals(sessionId, participant.getSessionId());
        assertEquals(userId, participant.getUserId());
        assertEquals(joinedAt, participant.getJoinedAt());
        assertEquals(createdAt, participant.getCreatedAt());
        assertEquals(updatedAt, participant.getUpdatedAt());
        assertEquals(createdBy, participant.getCreatedBy());
        assertEquals(updatedBy, participant.getUpdatedBy());
    }
}

