package com.geomeet.api.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvalidRegisterExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        InvalidRegisterException exception = new InvalidRegisterException();
        assertNotNull(exception);
        assertEquals("Invalid register info", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        String customMessage = "Custom error message";
        InvalidRegisterException exception = new InvalidRegisterException(customMessage);
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfDomainException() {
        InvalidRegisterException exception = new InvalidRegisterException();
        assertTrue(exception instanceof DomainException);
    }
}

