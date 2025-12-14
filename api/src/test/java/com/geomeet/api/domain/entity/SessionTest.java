package com.geomeet.api.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionTest {

    private Long initiatorId;

    @BeforeEach
    void setUp() {
        initiatorId = 1L;
    }

    @Test
    void shouldCreateSessionWithFactoryMethod() {
        Session session = Session.create(initiatorId);

        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertEquals(initiatorId, session.getInitiatorId());
        assertEquals(SessionStatus.ACTIVE, session.getStatus());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getUpdatedAt());
    }

    @Test
    void shouldGenerateUniqueSessionId() {
        Session session1 = Session.create(initiatorId);
        Session session2 = Session.create(initiatorId);

        assertNotEquals(session1.getSessionId(), session2.getSessionId());
    }

    @Test
    void shouldSetStatusToActiveWhenCreating() {
        Session session = Session.create(initiatorId);
        assertEquals(SessionStatus.ACTIVE, session.getStatus());
        assertTrue(session.isActive());
    }

    @Test
    void shouldReconstructSessionFromPersistence() {
        Long id = 1L;
        SessionId sessionId = SessionId.generate();
        SessionStatus status = SessionStatus.ACTIVE;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        String createdBy = "admin";
        String updatedBy = "admin";

        Session session = Session.reconstruct(
            id, sessionId, initiatorId, status, createdAt, updatedAt, createdBy, updatedBy
        );

        assertNotNull(session);
        assertEquals(id, session.getId());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(initiatorId, session.getInitiatorId());
        assertEquals(status, session.getStatus());
        assertEquals(createdAt, session.getCreatedAt());
        assertEquals(updatedAt, session.getUpdatedAt());
        assertEquals(createdBy, session.getCreatedBy());
        assertEquals(updatedBy, session.getUpdatedBy());
    }

    @Test
    void shouldEndSession() {
        Session session = Session.create(initiatorId);
        assertTrue(session.isActive());

        session.end();

        assertEquals(SessionStatus.ENDED, session.getStatus());
        assertFalse(session.isActive());
        assertNotNull(session.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenEndingAlreadyEndedSession() {
        Session session = Session.create(initiatorId);
        session.end();

        assertThrows(IllegalStateException.class, session::end);
    }

    @Test
    void shouldReturnTrueWhenSessionIsActive() {
        Session session = Session.create(initiatorId);
        assertTrue(session.isActive());
    }

    @Test
    void shouldReturnFalseWhenSessionIsNotActive() {
        Session session = Session.create(initiatorId);
        session.end();
        assertFalse(session.isActive());
    }

    @Test
    void shouldUpdateMeetingLocationSuccessfully() {
        // Given
        Session session = Session.create(initiatorId);
        Location newLocation = Location.of(1.3521, 103.8198);
        LocalDateTime originalUpdatedAt = session.getUpdatedAt();

        // When
        session.updateMeetingLocation(initiatorId, newLocation);

        // Then
        assertNotNull(session.getMeetingLocation());
        assertEquals(1.3521, session.getMeetingLocation().getLatitude().getValue());
        assertEquals(103.8198, session.getMeetingLocation().getLongitude().getValue());
        assertTrue(session.getUpdatedAt().isAfter(originalUpdatedAt) || 
                   session.getUpdatedAt().equals(originalUpdatedAt));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMeetingLocationForEndedSession() {
        // Given
        Session session = Session.create(initiatorId);
        session.end();
        Location newLocation = Location.of(1.3521, 103.8198);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            session.updateMeetingLocation(initiatorId, newLocation);
        });

        assertEquals("Cannot update meeting location for an ended session", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNonInitiatorTriesToUpdateMeetingLocation() {
        // Given
        Session session = Session.create(initiatorId);
        Long nonInitiatorUserId = 999L;
        Location newLocation = Location.of(1.3521, 103.8198);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            session.updateMeetingLocation(nonInitiatorUserId, newLocation);
        });

        assertEquals("Only the session initiator can update the meeting location", exception.getMessage());
        assertNull(session.getMeetingLocation());
    }

    @Test
    void shouldUpdateMeetingLocationMultipleTimes() {
        // Given
        Session session = Session.create(initiatorId);
        Location firstLocation = Location.of(1.3521, 103.8198);
        Location secondLocation = Location.of(1.2903, 103.8520);

        // When
        session.updateMeetingLocation(initiatorId, firstLocation);
        session.updateMeetingLocation(initiatorId, secondLocation);

        // Then
        assertNotNull(session.getMeetingLocation());
        assertEquals(1.2903, session.getMeetingLocation().getLatitude().getValue());
        assertEquals(103.8520, session.getMeetingLocation().getLongitude().getValue());
    }

    @Test
    void shouldReconstructSessionWithMeetingLocation() {
        // Given
        Long id = 1L;
        SessionId sessionId = SessionId.generate();
        SessionStatus status = SessionStatus.ACTIVE;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        String createdBy = "admin";
        String updatedBy = "admin";
        Location meetingLocation = Location.of(1.3521, 103.8198);

        // When
        Session session = Session.reconstruct(
            id, sessionId, initiatorId, status, createdAt, updatedAt, createdBy, updatedBy, meetingLocation
        );

        // Then
        assertNotNull(session);
        assertEquals(id, session.getId());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(initiatorId, session.getInitiatorId());
        assertEquals(status, session.getStatus());
        assertNotNull(session.getMeetingLocation());
        assertEquals(1.3521, session.getMeetingLocation().getLatitude().getValue());
        assertEquals(103.8198, session.getMeetingLocation().getLongitude().getValue());
    }

    @Test
    void shouldReconstructSessionWithoutMeetingLocation() {
        // Given
        Long id = 1L;
        SessionId sessionId = SessionId.generate();
        SessionStatus status = SessionStatus.ACTIVE;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        String createdBy = "admin";
        String updatedBy = "admin";

        // When
        Session session = Session.reconstruct(
            id, sessionId, initiatorId, status, createdAt, updatedAt, createdBy, updatedBy
        );

        // Then
        assertNotNull(session);
        assertNull(session.getMeetingLocation());
    }
}

