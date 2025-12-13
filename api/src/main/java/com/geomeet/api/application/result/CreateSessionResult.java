package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for create session use case.
 * Represents the output of the create session operation.
 */
@Getter
@Builder
public class CreateSessionResult {

    private final Long sessionId;
    private final String sessionIdString;
    private final Long initiatorId;
    private final String status;
    private final String createdAt;
}

