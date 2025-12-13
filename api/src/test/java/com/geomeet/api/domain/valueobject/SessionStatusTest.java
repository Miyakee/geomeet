package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SessionStatusTest {

    @Test
    void shouldCreateActiveStatus() {
        SessionStatus status = SessionStatus.ACTIVE;
        assertNotNull(status);
        assertEquals("Active", status.getValue());
    }

    @Test
    void shouldCreateInactiveStatus() {
        SessionStatus status = SessionStatus.INACTIVE;
        assertNotNull(status);
        assertEquals("Inactive", status.getValue());
    }

    @Test
    void shouldCreateEndedStatus() {
        SessionStatus status = SessionStatus.ENDED;
        assertNotNull(status);
        assertEquals("Ended", status.getValue());
    }

    @Test
    void shouldConvertStringToStatus() {
        assertEquals(SessionStatus.ACTIVE, SessionStatus.fromString("Active"));
        assertEquals(SessionStatus.INACTIVE, SessionStatus.fromString("Inactive"));
        assertEquals(SessionStatus.ENDED, SessionStatus.fromString("Ended"));
    }

    @Test
    void shouldConvertStringToStatusCaseInsensitive() {
        assertEquals(SessionStatus.ACTIVE, SessionStatus.fromString("active"));
        assertEquals(SessionStatus.INACTIVE, SessionStatus.fromString("INACTIVE"));
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> SessionStatus.fromString("Invalid"));
    }

    @Test
    void shouldThrowExceptionWhenStatusIsNull() {
        assertThrows(IllegalArgumentException.class, () -> SessionStatus.fromString(null));
    }

    @Test
    void shouldBeEqualWhenStatusesAreSame() {
        SessionStatus status1 = SessionStatus.ACTIVE;
        SessionStatus status2 = SessionStatus.ACTIVE;
        assertSame(status1, status2);
    }
}

