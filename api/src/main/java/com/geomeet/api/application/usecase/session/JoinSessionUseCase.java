package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.command.JoinSessionCommand;
import com.geomeet.api.application.result.JoinSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.InviteCode;
import com.geomeet.api.domain.valueobject.SessionId;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (Use Case) for joining a session.
 * Orchestrates the session joining flow.
 */
@Service
@AllArgsConstructor
public class JoinSessionUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;


    /**
     * Executes the join session use case.
     * Validates the session exists and is active, then adds the user as a participant.
     * If the user has already joined, returns the existing participant information.
     *
     * @param command the join session command containing session ID and user ID
     * @return join session result with participant details
     * @throws DomainException if session not found or session is ended
     */
    @Transactional
    public JoinSessionResult execute(JoinSessionCommand command) {
        // Find session by sessionId
        SessionId sessionId = SessionId.fromString(command.getSessionId());
        Session session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new DomainException("Invalid Session code"));

        // Verify invite code matches (security: prevents joining by guessing session ID)
        InviteCode providedInviteCode = InviteCode.fromString(command.getInviteCode());
        if (!session.getInviteCode().equals(providedInviteCode)) {
            throw new DomainException("Invalid invite code");
        }

        // Check if session is active (invitation link expires when session ends)
        if (!session.isActive()) {
            throw new DomainException("Cannot join a session that has ended");
        }

        // Check if user is already a participant
        Optional<SessionParticipant> existingParticipant = sessionParticipantRepository
            .findBySessionIdAndUserId(session.getId(), command.getUserId());
        
        if (existingParticipant.isPresent()) {
            // User has already joined, return existing participant information
            SessionParticipant participant = existingParticipant.get();
            return JoinSessionResult.builder()
                .participantId(participant.getId())
                .sessionId(session.getId())
                .sessionIdString(session.getSessionId().getValue())
                .userId(participant.getUserId())
                .joinedAt(participant.getJoinedAt().toString())
                .message("Already joined the session")
                .build();
        }

        // Create and save participant
        SessionParticipant participant = SessionParticipant.create(session.getId(), command.getUserId());
        SessionParticipant savedParticipant = sessionParticipantRepository.save(participant);

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

