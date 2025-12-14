package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for calculating optimal meeting location.
 * Represents the input for calculating optimal location use case.
 */
@Getter
@Builder
public class CalculateOptimalLocationCommand {

    private final String sessionId;
    private final Long userId;

    private CalculateOptimalLocationCommand(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public static CalculateOptimalLocationCommand of(String sessionId, Long userId) {
        return new CalculateOptimalLocationCommand(sessionId, userId);
    }
}

