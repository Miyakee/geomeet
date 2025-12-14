package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for ending a session.
 */
@Getter
@Builder
public class EndSessionResult {
    private final Long sessionId;
    private final String sessionIdString;
    private final String status;
    private final String endedAt;
    private final String message;
}

