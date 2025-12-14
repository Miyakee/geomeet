package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for get session details use case.
 * Represents the input for the get session details operation.
 */
@Getter
@Builder
public class GetSessionDetailsCommand {

    private final String sessionId;
    private final Long userId;

    public GetSessionDetailsCommand(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.sessionId = sessionId;
        this.userId = userId;
    }

    /**
     * Factory method to create a GetSessionDetailsCommand.
     *
     * @param sessionId the session ID string
     * @param userId the user ID requesting the details
     * @return a new GetSessionDetailsCommand
     */
    public static GetSessionDetailsCommand of(String sessionId, Long userId) {
        return GetSessionDetailsCommand.builder()
            .sessionId(sessionId)
            .userId(userId)
            .build();
    }
}

