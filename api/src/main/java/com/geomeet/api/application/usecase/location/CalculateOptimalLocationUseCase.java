package com.geomeet.api.application.usecase.location;

import com.geomeet.api.application.command.CalculateOptimalLocationCommand;
import com.geomeet.api.application.result.CalculateOptimalLocationResult;
import com.geomeet.api.application.usecase.session.BroadcastOptimalLocationUseCase;
import com.geomeet.api.application.usecase.session.SessionParticipantRepository;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.service.LocationCalculator;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (Use Case) for calculating optimal meeting location.
 * Orchestrates the optimal location calculation flow.
 */
@Service
@AllArgsConstructor
public class CalculateOptimalLocationUseCase {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final ParticipantLocationRepository participantLocationRepository;
    private final BroadcastOptimalLocationUseCase broadcastOptimalLocationUseCase;


    /**
     * Executes the calculate optimal location use case.
     * Calculates the geometric center of all participant locations.
     *
     * @param command the calculate optimal location command
     * @return calculate optimal location result with optimal coordinates
     * @throws GeomeetDomainException if session not found, insufficient participants, or access denied
     */
    @Transactional(readOnly = true)
    public CalculateOptimalLocationResult execute(CalculateOptimalLocationCommand command) {
        // Find session by sessionId
        SessionId sessionIdVO = SessionId.fromString(command.getSessionId());
        Session session = sessionRepository.findBySessionId(sessionIdVO)
            .orElseThrow(() -> new GeomeetDomainException("Access denied: User is not a participant or initiator"));

        // Check if session is active
        if (!session.isActive()) {
            throw new GeomeetDomainException("Cannot calculate optimal location for an ended session");
        }

        // Check if user is a participant or initiator
        boolean isParticipant = sessionParticipantRepository.existsBySessionIdAndUserId(
            session.getId(), command.getUserId()
        );
        if (!isParticipant && !session.getInitiatorId().equals(command.getUserId())) {
            throw new GeomeetDomainException("Access denied: User is not a participant or initiator");
        }

        // Get all participant locations for this session
        List<ParticipantLocation> participantLocations = participantLocationRepository.findBySessionId(session.getId());

        if (participantLocations.isEmpty()) {
            throw new GeomeetDomainException(
                    "No participant locations available. At least one participant must share their location.");
        }

        // Extract Location value objects
        List<Location> locations = participantLocations.stream()
            .map(ParticipantLocation::getLocation)
            .collect(Collectors.toList());

        // Calculate optimal location (geometric center)
        Location optimalLocation = LocationCalculator.calculateGeometricCenter(locations);

        // Calculate total travel distance
        double totalTravelDistance = LocationCalculator.calculateTotalTravelDistance(locations, optimalLocation);

        // Build result
        CalculateOptimalLocationResult result = CalculateOptimalLocationResult.builder()
            .sessionId(session.getId())
            .sessionIdString(session.getSessionId().getValue())
            .optimalLatitude(optimalLocation.getLatitude().getValue())
            .optimalLongitude(optimalLocation.getLongitude().getValue())
            .totalTravelDistance(totalTravelDistance)
            .participantCount(participantLocations.size())
            .message("Optimal location calculated successfully")
            .build();

        // Broadcast optimal location to all subscribers
        broadcastOptimalLocationUseCase.execute(result);

        return result;
    }
}

