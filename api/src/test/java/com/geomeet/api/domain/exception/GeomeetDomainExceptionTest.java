package com.geomeet.api.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GeomeetDomainExceptionTest {

    @Test
    void shouldCreateExceptionWithMessageOnly() {
        // Given
        String message = "Test error message";

        // When
        GeomeetDomainException exception = new GeomeetDomainException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus());
    }

    @Test
    void shouldCreateExceptionWithMessageAndHttpStatus() {
        // Given
        String message = "Test error message";
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        // When
        GeomeetDomainException exception = new GeomeetDomainException(message, httpStatus);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus());
    }

    @Test
    void shouldUseBadRequestWhenHttpStatusIsNull() {
        // Given
        String message = "Test error message";

        // When
        GeomeetDomainException exception = new GeomeetDomainException(message, null);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus());
    }

    @Test
    void shouldSupportDifferentHttpStatusCodes() {
        // Given
        String message = "Test error message";

        // When & Then
        GeomeetDomainException exception1 = new GeomeetDomainException(message, HttpStatus.UNAUTHORIZED);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), exception1.getHttpStatus());

        GeomeetDomainException exception2 = new GeomeetDomainException(message, HttpStatus.FORBIDDEN);
        assertEquals(HttpStatus.FORBIDDEN.value(), exception2.getHttpStatus());

        GeomeetDomainException exception3 = new GeomeetDomainException(message, HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception3.getHttpStatus());
    }
}

