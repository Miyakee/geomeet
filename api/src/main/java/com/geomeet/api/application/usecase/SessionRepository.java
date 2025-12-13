package com.geomeet.api.application.usecase;

import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.SessionId;
import java.util.Optional;

/**
 * Repository interface for Session aggregate.
 * This port is defined in the application layer (use case layer).
 * It defines what the use cases need from the infrastructure.
 */
public interface SessionRepository {

    /**
     * Saves a session (creates or updates).
     *
     * @param session the session aggregate root to save
     * @return the saved session
     */
    Session save(Session session);

    /**
     * Finds a session by session ID.
     *
     * @param sessionId the session ID
     * @return Optional containing the session if found
     */
    Optional<Session> findBySessionId(SessionId sessionId);

    /**
     * Finds a session by database ID.
     *
     * @param id the session database ID
     * @return Optional containing the session if found
     */
    Optional<Session> findById(Long id);
}

