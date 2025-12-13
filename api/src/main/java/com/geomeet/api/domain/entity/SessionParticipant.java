package com.geomeet.api.domain.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Session Participant entity.
 * Represents a user who has joined a session.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
public class SessionParticipant {

    private Long id;
    private Long sessionId;
    private Long userId;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Private constructor for reconstruction
    private SessionParticipant() {
    }

    /**
     * Factory method to create a new SessionParticipant.
     *
     * @param sessionId the session ID
     * @param userId the user ID who is joining
     * @return a new SessionParticipant
     */
    public static SessionParticipant create(Long sessionId, Long userId) {
        SessionParticipant participant = new SessionParticipant();
        participant.sessionId = sessionId;
        participant.userId = userId;
        LocalDateTime now = LocalDateTime.now();
        participant.joinedAt = now;
        participant.createdAt = now;
        participant.updatedAt = now;
        return participant;
    }

    /**
     * Factory method to reconstruct SessionParticipant from persistence.
     */
    public static SessionParticipant reconstruct(
        Long id,
        Long sessionId,
        Long userId,
        LocalDateTime joinedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
    ) {
        SessionParticipant participant = new SessionParticipant();
        participant.id = id;
        participant.sessionId = sessionId;
        participant.userId = userId;
        participant.joinedAt = joinedAt;
        participant.createdAt = createdAt;
        participant.updatedAt = updatedAt;
        participant.createdBy = createdBy;
        participant.updatedBy = updatedBy;
        return participant;
    }
}

