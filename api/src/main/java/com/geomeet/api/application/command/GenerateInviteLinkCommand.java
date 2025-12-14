package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for generate invite link use case.
 * Represents the input for the generate invite link operation.
 */
@Getter
@Builder
public class GenerateInviteLinkCommand {

    private final String sessionId;
    private final Long userId;

    public GenerateInviteLinkCommand(String sessionId, Long userId) {
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
     * Factory method to create a GenerateInviteLinkCommand.
     *
     * @param sessionId the session ID string
     * @param userId the user ID requesting the invite link
     * @return a new GenerateInviteLinkCommand
     */
    public static GenerateInviteLinkCommand of(String sessionId, Long userId) {
        return GenerateInviteLinkCommand.builder()
            .sessionId(sessionId)
            .userId(userId)
            .build();
    }
}

