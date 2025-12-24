package com.geomeet.api.adapter.web.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for building HTTP responses.
 * Reduces boilerplate code in controllers.
 */
public final class ResponseUtil {

    private ResponseUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a successful response with status 200 OK.
     */
    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    /**
     * Create a successful response with status 201 CREATED.
     */
    public static <T> ResponseEntity<T> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Create a response with custom status code.
     */
    public static <T> ResponseEntity<T> status(HttpStatus status, T body) {
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Create a no content response (204).
     */
    public static <T> ResponseEntity<T> noContent() {
        return ResponseEntity.noContent().build();
    }
}

