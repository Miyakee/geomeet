package com.geomeet.api.domain.entity;

import com.geomeet.api.domain.valueobject.InviteCode;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Session aggregate root.
 * Represents a meeting session that can be joined by multiple users.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
public class Session {

    private Long id;
    private SessionId sessionId;
    private InviteCode inviteCode; // Invitation code required to join the session
    private Long initiatorId; // User ID who created the session
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Location meetingLocation; // Meeting location set by initiator

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
        session.inviteCode = InviteCode.generate();
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
        InviteCode inviteCode,
        Long initiatorId,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Location meetingLocation
    ) {
        Session session = new Session();
        session.id = id;
        session.sessionId = sessionId;
        session.inviteCode = inviteCode;
        session.initiatorId = initiatorId;
        session.status = status;
        session.createdAt = createdAt;
        session.updatedAt = updatedAt;
        session.createdBy = createdBy;
        session.updatedBy = updatedBy;
        session.meetingLocation = meetingLocation;
        return session;
    }

    /**
     * Factory method to reconstruct Session from persistence (backward compatibility).
     * Meeting location will be null.
     */
    public static Session reconstruct(
        Long id,
        SessionId sessionId,
        InviteCode inviteCode,
        Long initiatorId,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
    ) {
        return reconstruct(id, sessionId, inviteCode, initiatorId, status, createdAt, updatedAt, createdBy, updatedBy, null);
    }

    /**
     * Factory method to reconstruct Session from persistence (backward compatibility for tests).
     * Generates a random invite code if null is provided.
     * WARNING: This should only be used in tests. Production code should always provide inviteCode.
     */
    public static Session reconstruct(
        Long id,
        SessionId sessionId,
        Long initiatorId,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Location meetingLocation
    ) {
        // Generate invite code for backward compatibility (tests only)
        InviteCode inviteCode = InviteCode.generate();
        return reconstruct(id, sessionId, inviteCode, initiatorId, status, createdAt, updatedAt, createdBy, updatedBy, meetingLocation);
    }

    /**
     * Factory method to reconstruct Session from persistence (backward compatibility for tests).
     * Generates a random invite code if null is provided.
     * WARNING: This should only be used in tests. Production code should always provide inviteCode.
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
        return reconstruct(id, sessionId, initiatorId, status, createdAt, updatedAt, createdBy, updatedBy, null);
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

    /**
     * Business method: Update meeting location.
     * Only the initiator can update the meeting location.
     *
     * @param location the new meeting location
     * @throws IllegalStateException if session is not active
     * @throws IllegalArgumentException if user is not the initiator
     */
    public void updateMeetingLocation(Long userId, Location location) {
        if (!this.isActive()) {
            throw new IllegalStateException("Cannot update meeting location for an ended session");
        }
        if (!this.initiatorId.equals(userId)) {
            throw new IllegalArgumentException("Only the session initiator can update the meeting location");
        }
        this.meetingLocation = location;
        this.updatedAt = LocalDateTime.now();
    }
}

