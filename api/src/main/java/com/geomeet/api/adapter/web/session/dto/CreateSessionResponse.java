package com.geomeet.api.adapter.web.session.dto;

/**
 * DTO for create session response.
 */
public class CreateSessionResponse {

    private Long id;
    private String sessionId;
    private Long initiatorId;
    private String status;
    private String createdAt;
    private String message;

    public CreateSessionResponse() {
    }

    public CreateSessionResponse(
        Long id,
        String sessionId,
        Long initiatorId,
        String status,
        String createdAt,
        String message
    ) {
        this.id = id;
        this.sessionId = sessionId;
        this.initiatorId = initiatorId;
        this.status = status;
        this.createdAt = createdAt;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

