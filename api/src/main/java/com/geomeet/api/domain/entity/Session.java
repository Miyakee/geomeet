package com.geomeet.api.domain.entity;

import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;

/**
 * Session aggregate root.
 * Represents a meeting session that can be joined by multiple users.
 */
public class Session {

    private Long id;
    private SessionId sessionId;
    private Long initiatorId; // User ID who created the session
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Private constructor for reconstruction
    private Session() {
    }

    /**
     * Factory method to create a new Session.
     *
     * @param initiatorId the user ID who is creating the session
     * @return a new Session with Active status
     */
    public static Session create(Long initiatorId) {
        Session session = new Session();
        session.sessionId = SessionId.generate();
        session.initiatorId = initiatorId;
        session.status = SessionStatus.ACTIVE;
        LocalDateTime now = LocalDateTime.now();
        session.createdAt = now;
        session.updatedAt = now;
        return session;
    }

    /**
     * Factory method to reconstruct Session from persistence.
     */
    public static Session reconstruct(
        Long id,
        SessionId sessionId,
        Long initiatorId,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
    ) {
        Session session = new Session();
        session.id = id;
        session.sessionId = sessionId;
        session.initiatorId = initiatorId;
        session.status = status;
        session.createdAt = createdAt;
        session.updatedAt = updatedAt;
        session.createdBy = createdBy;
        session.updatedBy = updatedBy;
        return session;
    }

    /**
     * Business method: End the session.
     */
    public void end() {
        if (this.status == SessionStatus.ENDED) {
            throw new IllegalStateException("Session is already ended");
        }
        this.status = SessionStatus.ENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Check if session is active.
     */
    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // Setters (only for infrastructure layer reconstruction)
    void setId(Long id) {
        this.id = id;
    }

    void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }

    void setStatus(SessionStatus status) {
        this.status = status;
    }

    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}

