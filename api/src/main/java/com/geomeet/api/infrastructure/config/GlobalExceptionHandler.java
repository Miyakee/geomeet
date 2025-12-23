package com.geomeet.api.infrastructure.config;

import com.geomeet.api.adapter.web.auth.dto.ErrorResponse;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.exception.InactiveUserExceptionGeomeet;
import com.geomeet.api.domain.exception.InvalidCredentialsExceptionGeomeet;
import com.geomeet.api.domain.exception.InvalidRegisterExceptionGeomeet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({InvalidRegisterExceptionGeomeet.class})
    public ResponseEntity<ErrorResponse> handleInvalidRegisterException(
        InvalidRegisterExceptionGeomeet ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("InvalidRegisterException: {} - Path: {}", ex.getMessage(), path, ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "register staff Failed",
                ex.getMessage(),
                path
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String path = request.getDescription(false).replace("uri=", "");
        String message = "Validation failed: " + errors.toString();
        logger.warn("Validation error: {} - Path: {}", message, path);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                path
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("IllegalArgumentException: {} - Path: {}", ex.getMessage(), path, ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Argument",
                ex.getMessage(),
                path
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler({InvalidCredentialsExceptionGeomeet.class, InactiveUserExceptionGeomeet.class})
    public ResponseEntity<ErrorResponse> handleDomainExceptions(
            GeomeetDomainException ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Authentication failed: {} - Path: {}", ex.getMessage(), path);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                ex.getMessage(),
                path
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(GeomeetDomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            GeomeetDomainException ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Domain exception: {} - Path: {}", ex.getMessage(), path, ex);

        if (ex.getMessage() != null && ex.getMessage().contains("Access denied")) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    HttpStatus.FORBIDDEN.value(),
                    "Access Denied",
                    ex.getMessage(),
                    path
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Domain Error",
                ex.getMessage(),
                path
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.error("Unexpected error occurred - Path: {}", path, ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ops...Please try later",
                path
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

