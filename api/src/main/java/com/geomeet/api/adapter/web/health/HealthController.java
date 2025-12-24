package com.geomeet.api.adapter.web.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Web adapter (controller) for health check endpoints.
 * This is the entry point for health monitoring.
 */
@RestController
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @Operation(
        summary = "Health check",
        description = "Check if the API is running and healthy"
    )
    @ApiResponse(responseCode = "200", description = "API is healthy")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "message", "Spring Boot application is running"
        );
    }

    @Operation(
        summary = "Root endpoint",
        description = "Welcome message for the API"
    )
    @ApiResponse(responseCode = "200", description = "Welcome message")
    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of("message", "Welcome to GeoMeet API");
    }
}

