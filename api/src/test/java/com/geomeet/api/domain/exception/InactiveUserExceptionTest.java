package com.geomeet.api.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InactiveUserExceptionTest {

    @Test
    void shouldCreateExceptionWithDefaultMessage() {
        InactiveUserExceptionGeomeet exception = new InactiveUserExceptionGeomeet();
        assertNotNull(exception);
        assertEquals("User account is inactive", exception.getMessage());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        String customMessage = "Custom error message";
        InactiveUserExceptionGeomeet exception = new InactiveUserExceptionGeomeet(customMessage);
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfDomainException() {
        InactiveUserExceptionGeomeet exception = new InactiveUserExceptionGeomeet();
        assertTrue(exception instanceof GeomeetDomainException);
    }
}

