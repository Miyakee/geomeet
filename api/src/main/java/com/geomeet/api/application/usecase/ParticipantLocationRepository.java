package com.geomeet.api.application.usecase;

import com.geomeet.api.domain.entity.ParticipantLocation;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParticipantLocation.
 * This port is defined in the application layer (use case layer).
 */
public interface ParticipantLocationRepository {

    /**
     * Saves a participant location (creates or updates).
     *
     * @param location the participant location to save
     * @return the saved participant location
     */
    ParticipantLocation save(ParticipantLocation location);

    /**
     * Finds a location by participant ID.
     *
     * @param participantId the participant ID
     * @return Optional containing the location if found
     */
    Optional<ParticipantLocation> findByParticipantId(Long participantId);

    /**
     * Finds a location by session ID and user ID.
     *
     * @param sessionId the session ID
     * @param userId the user ID
     * @return Optional containing the location if found
     */
    Optional<ParticipantLocation> findBySessionIdAndUserId(Long sessionId, Long userId);

    /**
     * Finds all locations for a session.
     *
     * @param sessionId the session ID
     * @return list of locations
     */
    List<ParticipantLocation> findBySessionId(Long sessionId);
}

