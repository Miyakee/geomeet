package com.geomeet.api.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.geomeet.api.adapter.web.auth.dto.ErrorResponse;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.exception.InactiveUserException;
import com.geomeet.api.domain.exception.InvalidCredentialsException;
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
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

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
        InactiveUserException ex = new InactiveUserException("User is inactive");

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
        DomainException ex = new DomainException("Domain error occurred");

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

