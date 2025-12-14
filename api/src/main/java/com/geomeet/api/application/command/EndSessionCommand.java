package com.geomeet.api.application.command;

import lombok.Getter;

/**
 * Command object for ending a session.
 */
@Getter
public class EndSessionCommand {

    private final String sessionId;
    private final Long userId;

    private EndSessionCommand(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public static EndSessionCommand of(String sessionId, Long userId) {
        return new EndSessionCommand(sessionId, userId);
    }
}

