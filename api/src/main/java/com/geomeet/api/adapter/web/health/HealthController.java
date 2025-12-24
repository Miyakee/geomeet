package com.geomeet.api.adapter.web.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Web adapter (controller) for health check endpoints.
 * This is the entry point for health monitoring.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "message", "Spring Boot application is running"
        );
    }

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of("message", "Welcome to GeoMeet API");
    }
}

