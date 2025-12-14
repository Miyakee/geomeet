package com.geomeet.api.application.usecase;

import com.geomeet.api.application.command.EndSessionCommand;
import com.geomeet.api.application.result.EndSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.SessionId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (Use Case) for ending a session.
 * Orchestrates the session end flow.
 */
@Service
public class EndSessionUseCase {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SessionRepository sessionRepository;

    public EndSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Executes the end session use case.
     * Only the session initiator can end the session.
     *
     * @param command the end session command
     * @return end session result
     * @throws DomainException if session not found, user is not initiator, or session is already ended
     */
    @Transactional
    public EndSessionResult execute(EndSessionCommand command) {
        // Find session by sessionId
        SessionId sessionIdVO = SessionId.fromString(command.getSessionId());
        Session session = sessionRepository.findBySessionId(sessionIdVO)
            .orElseThrow(() -> new DomainException("Session not found"));

        // Check if user is the initiator
        if (!session.getInitiatorId().equals(command.getUserId())) {
            throw new DomainException("Only the session initiator can end the session");
        }

        // End the session (this will validate that session is not already ended)
        session.end();

        // Save session with atomic status update
        Session savedSession = sessionRepository.save(session);

        // Build result
        return EndSessionResult.builder()
            .sessionId(savedSession.getId())
            .sessionIdString(savedSession.getSessionId().getValue())
            .status(savedSession.getStatus().getValue())
            .endedAt(savedSession.getUpdatedAt().format(DATE_TIME_FORMATTER))
            .message("Session ended successfully")
            .build();
    }
}

