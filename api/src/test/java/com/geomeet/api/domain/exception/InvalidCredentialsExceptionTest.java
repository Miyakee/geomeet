package com.geomeet.api.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvalidCredentialsExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        assertNotNull(exception);
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        String customMessage = "Custom error message";
        InvalidCredentialsException exception = new InvalidCredentialsException(customMessage);
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfDomainException() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        assertTrue(exception instanceof DomainException);
    }
}

