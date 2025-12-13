package com.geomeet.api.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}

