package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.command.GenerateInviteLinkCommand;
import com.geomeet.api.application.result.GenerateInviteLinkResult;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.SessionId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for generating invitation link.
 * Orchestrates the invite link generation flow.
 */
@Service
@AllArgsConstructor
public class GenerateInviteLinkUseCase {

    private final SessionRepository sessionRepository;

    /**
     * Executes the generate invite link use case.
     * Validates that the user is the session initiator and generates the invite link.
     *
     * @param command the generate invite link command
     * @return generate invite link result with link and code
     * @throws DomainException if session not found or user is not the initiator
     */
    public GenerateInviteLinkResult execute(GenerateInviteLinkCommand command) {
        // Find session by sessionId
        SessionId sessionIdVO = SessionId.fromString(command.getSessionId());
        Session session = sessionRepository.findBySessionId(sessionIdVO)
            .orElseThrow(() -> new DomainException("Session not found"));

        // Verify user is the initiator
        if (!session.getInitiatorId().equals(command.getUserId())) {
            throw new DomainException("Only the session initiator can generate invite links");
        }

        // Generate invite link (using sessionId as the code)
        String inviteLink = "/join?sessionId=" + command.getSessionId();
        String inviteCode = command.getSessionId();

        // Return result
        return GenerateInviteLinkResult.builder()
            .sessionId(command.getSessionId())
            .inviteLink(inviteLink)
            .inviteCode(inviteCode)
            .build();
    }
}

