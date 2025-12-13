package com.geomeet.api.application.usecase;

import com.geomeet.api.domain.entity.SessionParticipant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SessionParticipant aggregate.
 * This port is defined in the application layer (use case layer).
 */
public interface SessionParticipantRepository {

    /**
     * Saves a session participant (creates or updates).
     * @param participant the session participant to save
     * @return the saved session participant
     */
    SessionParticipant save(SessionParticipant participant);

    /**
     * Finds all participants for a session.
     * @param sessionId the session ID
     * @return list of participants
     */
    List<SessionParticipant> findBySessionId(Long sessionId);

    /**
     * Finds a participant by session ID and user ID.
     * @param sessionId the session ID
     * @param userId the user ID
     * @return Optional containing the participant if found
     */
    Optional<SessionParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);

    /**
     * Checks if a user has already joined a session.
     * @param sessionId the session ID
     * @param userId the user ID
     * @return true if the user has joined, false otherwise
     */
    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);

    /**
     * Counts the number of participants in a session.
     * @param sessionId the session ID
     * @return the count of participants
     */
    long countBySessionId(Long sessionId);
}

