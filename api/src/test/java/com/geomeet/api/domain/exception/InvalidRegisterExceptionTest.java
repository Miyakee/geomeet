package com.geomeet.api.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvalidRegisterExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        InvalidRegisterExceptionGeomeet exception = new InvalidRegisterExceptionGeomeet();
        assertNotNull(exception);
        assertEquals("Invalid register info", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        String customMessage = "Custom error message";
        InvalidRegisterExceptionGeomeet exception = new InvalidRegisterExceptionGeomeet(customMessage);
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfDomainException() {
        InvalidRegisterExceptionGeomeet exception = new InvalidRegisterExceptionGeomeet();
        assertTrue(exception instanceof GeomeetDomainException);
    }
}

