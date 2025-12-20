package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.command.CreateSessionCommand;
import com.geomeet.api.application.result.CreateSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (Use Case) for creating a session.
 * Orchestrates the session creation flow.
 */
@Service
@AllArgsConstructor
public class CreateSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;


    /**
     * Executes the create session use case.
     * Creates a new session with a unique session ID and Active status.
     * Also creates a participant record for the initiator so they can update location.
     *
     * @param command the create session command containing initiator ID
     * @return create session result with session details
     */
    @Transactional
    public CreateSessionResult execute(CreateSessionCommand command) {
        // Create new session using domain factory method
        Session session = Session.create(command.getInitiatorId());

        // Save session
        Session savedSession = sessionRepository.save(session);

        // Create a participant record for the initiator
        // This ensures the initiator is treated as a participant and can update location
        SessionParticipant initiatorParticipant = SessionParticipant.create(
            savedSession.getId(),
            savedSession.getInitiatorId()
        );
        sessionParticipantRepository.save(initiatorParticipant);

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

