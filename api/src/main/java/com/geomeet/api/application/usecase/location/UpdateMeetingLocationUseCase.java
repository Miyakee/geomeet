package com.geomeet.api.application.usecase.location;

import com.geomeet.api.application.command.UpdateMeetingLocationCommand;
import com.geomeet.api.application.result.UpdateMeetingLocationResult;
import com.geomeet.api.application.usecase.session.BroadcastMeetingLocationUseCase;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (Use Case) for updating meeting location by initiator.
 * Orchestrates the meeting location update flow.
 */
@Service
@AllArgsConstructor
public class UpdateMeetingLocationUseCase {

    private final SessionRepository sessionRepository;
    private final BroadcastMeetingLocationUseCase broadcastMeetingLocationUseCase;

    /**
     * Executes the update meeting location use case.
     * Only the session initiator can update the meeting location.
     *
     * @param command the update meeting location command
     * @return update meeting location result
     * @throws DomainException if session not found, user is not initiator, or session is ended
     */
    @Transactional
    public UpdateMeetingLocationResult execute(UpdateMeetingLocationCommand command) {
        // Find session by sessionId
        SessionId sessionIdVO = SessionId.fromString(command.getSessionId());
        Session session = sessionRepository.findBySessionId(sessionIdVO)
            .orElseThrow(() -> new DomainException("Session not found"));

        // Check if session is active
        if (!session.isActive()) {
            throw new DomainException("Cannot update meeting location for an ended session");
        }

        // Create location value object
        Location location = Location.of(command.getLatitude(), command.getLongitude());

        // Update meeting location (this will validate that user is the initiator)
        session.updateMeetingLocation(command.getUserId(), location);

        // Save session
        Session savedSession = sessionRepository.save(session);

        // Build result
        UpdateMeetingLocationResult result = UpdateMeetingLocationResult.builder()
            .sessionId(savedSession.getId())
            .sessionIdString(savedSession.getSessionId().getValue())
            .latitude(savedSession.getMeetingLocation().getLatitude().getValue())
            .longitude(savedSession.getMeetingLocation().getLongitude().getValue())
            .message("Meeting location updated successfully")
            .build();

        // Broadcast meeting location update to all subscribers
        broadcastMeetingLocationUseCase.execute(result);

        return result;
    }
}

