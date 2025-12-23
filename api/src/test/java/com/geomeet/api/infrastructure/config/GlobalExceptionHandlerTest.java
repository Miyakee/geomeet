package com.geomeet.api.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.geomeet.api.adapter.web.auth.dto.ErrorResponse;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.exception.InactiveUserExceptionGeomeet;
import com.geomeet.api.domain.exception.InvalidCredentialsExceptionGeomeet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void shouldHandleValidationExceptions() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        FieldError fieldError = new FieldError("object", "field", "default message");
        errors.add(fieldError);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
    }

    @Test
    void shouldHandleInvalidCredentialsException() {
        // Given
        InvalidCredentialsExceptionGeomeet ex = new InvalidCredentialsExceptionGeomeet("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDomainExceptions(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Authentication Failed", response.getBody().getError());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void shouldHandleInactiveUserException() {
        // Given
        InactiveUserExceptionGeomeet ex = new InactiveUserExceptionGeomeet("User is inactive");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDomainExceptions(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Authentication Failed", response.getBody().getError());
        assertEquals("User is inactive", response.getBody().getMessage());
    }

    @Test
    void shouldHandleDomainException() {
        // Given
        GeomeetDomainException ex = new GeomeetDomainException("Domain error occurred",HttpStatus.BAD_REQUEST);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDomainException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Domain Error", response.getBody().getError());
        assertEquals("Domain error occurred", response.getBody().getMessage());
    }

    @Test
    void shouldHandleDomainExceptionWithHttpStatus() {
        // Given - Exception with explicit HTTP status code
        GeomeetDomainException ex = new GeomeetDomainException("Resource not found", HttpStatus.NOT_FOUND);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDomainException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void shouldHandleDomainExceptionWithForbiddenStatus() {
        // Given - Exception with FORBIDDEN status
        GeomeetDomainException ex = new GeomeetDomainException("Access denied", HttpStatus.FORBIDDEN);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDomainException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
        assertEquals("Access Denied", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
    }


    @Test
    void shouldHandleGenericException() {
        // Given
        Exception ex = new Exception("Internal server error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Internal server error", response.getBody().getMessage());
    }
}

