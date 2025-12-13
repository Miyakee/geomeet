package com.geomeet.api.application.usecase;

import com.geomeet.api.application.command.CreateSessionCommand;
import com.geomeet.api.application.result.CreateSessionResult;
import com.geomeet.api.domain.entity.Session;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for creating a session.
 * Orchestrates the session creation flow.
 */
@Service
public class CreateSessionUseCase {

    private final SessionRepository sessionRepository;

    public CreateSessionUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Executes the create session use case.
     * Creates a new session with a unique session ID and Active status.
     *
     * @param command the create session command containing initiator ID
     * @return create session result with session details
     */
    public CreateSessionResult execute(CreateSessionCommand command) {
        // Create new session using domain factory method
        Session session = Session.create(command.getInitiatorId());

        // Save session
        Session savedSession = sessionRepository.save(session);

        // Return result
        return CreateSessionResult.builder()
            .sessionId(savedSession.getId())
            .sessionIdString(savedSession.getSessionId().getValue())
            .initiatorId(savedSession.getInitiatorId())
            .status(savedSession.getStatus().getValue())
            .createdAt(savedSession.getCreatedAt().toString())
            .build();
    }
}

