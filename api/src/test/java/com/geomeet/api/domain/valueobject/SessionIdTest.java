package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SessionIdTest {

    @Test
    void shouldGenerateUniqueSessionId() {
        SessionId sessionId1 = SessionId.generate();
        SessionId sessionId2 = SessionId.generate();

        assertNotNull(sessionId1);
        assertNotNull(sessionId2);
        assertNotEquals(sessionId1, sessionId2);
    }

    @Test
    void shouldCreateSessionIdFromString() {
        String uuid = "test-session-id";
        SessionId sessionId = SessionId.fromString(uuid);

        assertNotNull(sessionId);
        assertEquals(uuid, sessionId.getValue());
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->  SessionId.fromString(null));
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->  SessionId.fromString(""));
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->  SessionId.fromString("   "));
    }

    @Test
    void shouldBeEqualWhenSessionIdsAreSame() {
        String uuid = "test-session-id";
        SessionId sessionId1 = SessionId.fromString(uuid);
        SessionId sessionId2 = SessionId.fromString(uuid);

        assertEquals(sessionId1, sessionId2);
        assertEquals(sessionId1.hashCode(), sessionId2.hashCode());
    }
}

