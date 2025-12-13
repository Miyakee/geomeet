package com.geomeet.api.application.result;

/**
 * Result object for create session use case.
 * Represents the output of the create session operation.
 */
public class CreateSessionResult {

    private final Long sessionId;
    private final String sessionIdString;
    private final Long initiatorId;
    private final String status;
    private final String createdAt;

    public CreateSessionResult(
        Long sessionId,
        String sessionIdString,
        Long initiatorId,
        String status,
        String createdAt
    ) {
        this.sessionId = sessionId;
        this.sessionIdString = sessionIdString;
        this.initiatorId = initiatorId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getSessionIdString() {
        return sessionIdString;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

