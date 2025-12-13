package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for join session use case.
 * Represents the output after joining a session.
 */
@Getter
@Builder
public class JoinSessionResult {

    private final Long participantId;
    private final Long sessionId;
    private final String sessionIdString;
    private final Long userId;
    private final String joinedAt;
    private final String message;
}

