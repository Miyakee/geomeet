package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for join session use case.
 * Represents the input for joining a session.
 */
@Getter
@Builder
public class JoinSessionCommand {

    private final String sessionId; // SessionId value (UUID string)
    private final String inviteCode; // Invitation code required to join
    private final Long userId; // User ID who wants to join

    public JoinSessionCommand(String sessionId, String inviteCode, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new IllegalArgumentException("Invite code cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.sessionId = sessionId;
        this.inviteCode = inviteCode;
        this.userId = userId;
    }

    public static JoinSessionCommand of(String sessionId, String inviteCode, Long userId) {
        return JoinSessionCommand.builder()
            .sessionId(sessionId)
            .inviteCode(inviteCode)
            .userId(userId)
            .build();
    }
}

