package com.geomeet.api.application.usecase;

import com.geomeet.api.application.command.JoinSessionCommand;
import com.geomeet.api.application.result.JoinSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.SessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (Use Case) for joining a session.
 * Orchestrates the session joining flow.
 */
@Service
public class JoinSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;

    public JoinSessionUseCase(
        SessionRepository sessionRepository,
        SessionParticipantRepository sessionParticipantRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.sessionParticipantRepository = sessionParticipantRepository;
    }

    /**
     * Executes the join session use case.
     * Validates the session exists and is active, then adds the user as a participant.
     *
     * @param command the join session command containing session ID and user ID
     * @return join session result with participant details
     * @throws DomainException if session not found, session is ended, or user already joined
     */
    @Transactional
    public JoinSessionResult execute(JoinSessionCommand command) {
        // Find session by sessionId
        SessionId sessionId = SessionId.fromString(command.getSessionId());
        Session session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new DomainException("Session not found"));

        // Check if session is active (invitation link expires when session ends)
        if (!session.isActive()) {
            throw new DomainException("Cannot join a session that has ended");
        }

        // Check if user is already a participant
        if (sessionParticipantRepository.existsBySessionIdAndUserId(session.getId(), command.getUserId())) {
            throw new DomainException("User has already joined this session");
        }

        // Create and save participant
        SessionParticipant participant = SessionParticipant.create(session.getId(), command.getUserId());
        SessionParticipant savedParticipant = sessionParticipantRepository.save(participant);

        // Return result
        return JoinSessionResult.builder()
            .participantId(savedParticipant.getId())
            .sessionId(session.getId())
            .sessionIdString(session.getSessionId().getValue())
            .userId(savedParticipant.getUserId())
            .joinedAt(savedParticipant.getJoinedAt().toString())
            .message("Successfully joined the session")
            .build();
    }
}

