package com.geomeet.api.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvalidCredentialsExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        InvalidCredentialsExceptionGeomeet exception = new InvalidCredentialsExceptionGeomeet();
        assertNotNull(exception);
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        String customMessage = "Custom error message";
        InvalidCredentialsExceptionGeomeet exception = new InvalidCredentialsExceptionGeomeet(customMessage);
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfDomainException() {
        InvalidCredentialsExceptionGeomeet exception = new InvalidCredentialsExceptionGeomeet();
        assertTrue(exception instanceof GeomeetDomainException);
    }
}

