package com.geomeet.api.infrastructure.config;

import com.geomeet.api.adapter.web.auth.dto.ErrorResponse;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.exception.InactiveUserExceptionGeomeet;
import com.geomeet.api.domain.exception.InvalidCredentialsExceptionGeomeet;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        logger.warn("Geomeet DomainException exception: {} - Path: {}", ex.getMessage(), path, ex);

        // Get error title based on HTTP status code
        HttpStatus httpStatus = HttpStatus.resolve(ex.getHttpStatus());
        String errorTitle = httpStatus != null ? getErrorTitleForStatus(httpStatus) : "Error";
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getHttpStatus(),
                errorTitle,
                ex.getMessage(),
                path
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * Get error title based on HTTP status code.
     * 
     * @param httpStatus the HTTP status code
     * @return the error title string
     */
    private String getErrorTitleForStatus(HttpStatus httpStatus) {
        if (httpStatus == HttpStatus.BAD_REQUEST) {
            return "Bad Request";
        } else if (httpStatus == HttpStatus.UNAUTHORIZED) {
            return "Authentication Failed";
        } else if (httpStatus == HttpStatus.FORBIDDEN) {
            return "Access Denied";
        } else if (httpStatus == HttpStatus.NOT_FOUND) {
            return "Not Found";
        } else if (httpStatus == HttpStatus.CONFLICT) {
            return "Conflict";
        } else if (httpStatus == HttpStatus.UNPROCESSABLE_ENTITY) {
            return "Unprocessable Entity";
        } else if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            return "Internal Server Error";
        } else {
            return "Error";
        }
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

