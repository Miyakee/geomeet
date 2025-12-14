package com.geomeet.api.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * WebSocket security configuration.
 * In production, should implement proper authentication for WebSocket connections.
 */
@Configuration
public class WebSocketSecurityConfig {
    // WebSocket security can be configured here if needed
    // For now, we rely on the HTTP authentication before WebSocket upgrade
}

