package com.geomeet.api.application.usecase.location;

import com.geomeet.api.application.command.UpdateLocationCommand;
import com.geomeet.api.application.result.UpdateLocationResult;
import com.geomeet.api.application.usecase.session.BroadcastLocationUpdateUseCase;
import com.geomeet.api.application.usecase.session.SessionParticipantRepository;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (Use Case) for updating participant location.
 * Orchestrates the location update flow.
 */
@Service
@AllArgsConstructor
public class UpdateLocationUseCase {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final ParticipantLocationRepository participantLocationRepository;
    private final BroadcastLocationUpdateUseCase broadcastLocationUpdateUseCase;


    /**
     * Executes the update location use case.
     * Validates the session and participant, then updates or creates the location.
     *
     * @param command the update location command
     * @return update location result with location details
     * @throws GeomeetDomainException if session not found, participant not found, or access denied
     */
    @Transactional
    public UpdateLocationResult execute(UpdateLocationCommand command) {
        // Find session by sessionId
        SessionId sessionIdVO = SessionId.fromString(command.getSessionId());
        Session session = sessionRepository.findBySessionId(sessionIdVO)
            .orElseThrow(() -> new GeomeetDomainException("Session not found"));

        // Check if session is active
        if (!session.isActive()) {
            throw new GeomeetDomainException("Cannot update location for an ended session");
        }

        // Find participant (initiator should also have a participant record)
        SessionParticipant participant = sessionParticipantRepository
            .findBySessionIdAndUserId(session.getId(), command.getUserId())
            .orElseThrow(() -> new GeomeetDomainException("User is not a participant in this session"));

        // Create location value object
        Location location = Location.of(
            command.getLatitude(),
            command.getLongitude(),
            command.getAccuracy()
        );

        // Find existing location or create new one
        ParticipantLocation participantLocation = participantLocationRepository
            .findByParticipantId(participant.getId())
            .orElse(null);

        if (participantLocation == null) {
            // Create new location
            participantLocation = ParticipantLocation.create(
                participant.getId(),
                session.getId(),
                command.getUserId(),
                location
            );
        } else {
            // Update existing location
            participantLocation.updateLocation(location);
        }

        // Save location
        ParticipantLocation savedLocation = participantLocationRepository.save(participantLocation);

        // Build result
        UpdateLocationResult result = UpdateLocationResult.builder()
            .participantId(savedLocation.getParticipantId())
            .sessionId(savedLocation.getSessionId())
            .sessionIdString(session.getSessionId().getValue())
            .userId(savedLocation.getUserId())
            .latitude(savedLocation.getLocation().getLatitude().getValue())
            .longitude(savedLocation.getLocation().getLongitude().getValue())
            .accuracy(savedLocation.getLocation().getAccuracy())
            .updatedAt(savedLocation.getUpdatedAt().format(DATE_TIME_FORMATTER))
            .message("Location updated successfully")
            .build();

        // Broadcast location update to all subscribers
        broadcastLocationUpdateUseCase.execute(result);

        return result;
    }
}

