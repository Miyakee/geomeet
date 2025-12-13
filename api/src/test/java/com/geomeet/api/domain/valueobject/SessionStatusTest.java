package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
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
        assertEquals(SessionStatus.ENDED, SessionStatus.fromString("ended"));
        assertEquals(SessionStatus.ACTIVE, SessionStatus.fromString("ACTIVE"));
        assertEquals(SessionStatus.INACTIVE, SessionStatus.fromString("inactive"));
        assertEquals(SessionStatus.ENDED, SessionStatus.fromString("ENDED"));
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
    void shouldThrowExceptionWhenStatusIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> SessionStatus.fromString(""));
    }

    @Test
    void shouldThrowExceptionWhenStatusIsBlank() {
        // fromString checks null first, so blank strings go to default case
        assertThrows(IllegalArgumentException.class, () -> SessionStatus.fromString("   "));
    }

    @Test
    void shouldThrowExceptionWhenConstructorReceivesBlankValue() throws Exception {
        // Test the private constructor's isBlank() check using reflection
        Constructor<SessionStatus> constructor = SessionStatus.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        
        // Test with blank string - InvocationTargetException wraps IllegalArgumentException
        Exception exception1 = assertThrows(Exception.class, () -> constructor.newInstance("   "));
        assertTrue(exception1.getCause() instanceof IllegalArgumentException ||
                   exception1 instanceof IllegalArgumentException);
        
        // Test with empty string
        Exception exception2 = assertThrows(Exception.class, () -> constructor.newInstance(""));
        assertTrue(exception2.getCause() instanceof IllegalArgumentException ||
                   exception2 instanceof IllegalArgumentException);
        
        // Test with null
        Exception exception3 = assertThrows(Exception.class, () -> constructor.newInstance((String) null));
        assertTrue(exception3.getCause() instanceof IllegalArgumentException ||
                   exception3 instanceof IllegalArgumentException);
    }

    @Test
    void shouldBeEqualWhenStatusesAreSame() {
        SessionStatus status1 = SessionStatus.ACTIVE;
        SessionStatus status2 = SessionStatus.ACTIVE;
        assertSame(status1, status2);
    }
}

