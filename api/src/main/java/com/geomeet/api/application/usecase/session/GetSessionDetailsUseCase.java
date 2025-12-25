package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.command.GetSessionDetailsCommand;
import com.geomeet.api.application.result.GetSessionDetailsResult;
import com.geomeet.api.application.usecase.auth.UserRepository;
import com.geomeet.api.application.usecase.location.ParticipantLocationRepository;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.exception.ErrorCode;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.valueobject.SessionId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for getting session details.
 * Orchestrates the session details retrieval flow.
 */
@Service
@AllArgsConstructor
public class GetSessionDetailsUseCase {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final UserRepository userRepository;
    private final ParticipantLocationRepository participantLocationRepository;


    /**
     * Executes the get session details use case.
     * Retrieves session information and all participants.
     * 
     * Security: Returns "Access denied" for both non-existent sessions and unauthorized access
     * to prevent information disclosure through enumeration attacks.
     *
     * @param command the get session details command
     * @return session details result with participants
     * @throws GeomeetDomainException if session not found, user not found, or access denied
     */
    public GetSessionDetailsResult execute(GetSessionDetailsCommand command) {
        // Find session by sessionId
        SessionId sessionIdVO = SessionId.fromString(command.getSessionId());
        Optional<Session> sessionOpt = sessionRepository.findBySessionId(sessionIdVO);
        
        // Security: If session doesn't exist, return "Access denied" instead of "Session not found"
        // to prevent information disclosure through enumeration attacks
        if (sessionOpt.isEmpty()) {
            throw ErrorCode.ACCESS_DENIED.toException();
        }
        
        Session session = sessionOpt.get();

        // Check if user is a participant or initiator
        boolean isParticipant = sessionParticipantRepository.existsBySessionIdAndUserId(
            session.getId(), command.getUserId()
        );
        if (!isParticipant && !session.getInitiatorId().equals(command.getUserId())) {
            throw ErrorCode.ACCESS_DENIED.toException();
        }

        // Get initiator info
        User initiator = userRepository.findById(session.getInitiatorId())
            .orElseThrow(() -> ErrorCode.INITIATOR_NOT_FOUND.toException());

        // Get all participant locations for this session (create a map for quick lookup)
        List<ParticipantLocation> participantLocations = participantLocationRepository.findBySessionId(session.getId());
        java.util.Map<Long, ParticipantLocation> locationMap = participantLocations.stream()
            .collect(Collectors.toMap(
                ParticipantLocation::getUserId,
                location -> location,
                (existing, replacement) -> existing // Keep first if duplicate
            ));

        // Get all participants
        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionId(session.getId());
        List<GetSessionDetailsResult.ParticipantInfo> participantInfos = participants.stream()
            .map(participant -> {
                User user = userRepository.findById(participant.getUserId())
                    .orElseThrow(() -> ErrorCode.USER_NOT_FOUND_FOR_PARTICIPANT.toException());
                
                // Get location for this participant if available
                ParticipantLocation location = locationMap.get(participant.getUserId());
                
                GetSessionDetailsResult.ParticipantInfo.ParticipantInfoBuilder builder =
                    GetSessionDetailsResult.ParticipantInfo.builder()
                    .participantId(participant.getId())
                    .userId(participant.getUserId())
                    .username(user.getUsername().getValue())
                    .email(user.getEmail().getValue())
                    .joinedAt(participant.getJoinedAt().format(DATE_TIME_FORMATTER));
                
                // Add location information if available
                if (location != null) {
                    builder.latitude(location.getLocation().getLatitude().getValue())
                        .longitude(location.getLocation().getLongitude().getValue())
                        .accuracy(location.getLocation().getAccuracy() != null 
                            ? location.getLocation().getAccuracy() : null)
                        .locationUpdatedAt(location.getUpdatedAt() != null 
                            ? location.getUpdatedAt().format(DATE_TIME_FORMATTER) : null);
                }
                
                return builder.build();
            })
            .collect(Collectors.toList());

        // Check if initiator is in the participants list, if not, add them
        boolean initiatorInParticipants = participantInfos.stream()
            .anyMatch(p -> p.getUserId().equals(session.getInitiatorId()));
        
        if (!initiatorInParticipants) {
            // Get location for initiator if available
            ParticipantLocation initiatorLocation = locationMap.get(session.getInitiatorId());
            
            GetSessionDetailsResult.ParticipantInfo.ParticipantInfoBuilder builder =
                GetSessionDetailsResult.ParticipantInfo.builder()
                .participantId(null) // Initiator doesn't have a participant ID
                .userId(session.getInitiatorId())
                .username(initiator.getUsername().getValue())
                .email(initiator.getEmail().getValue())
                .joinedAt(session.getCreatedAt().format(DATE_TIME_FORMATTER));
            
            // Add location information if available
            if (initiatorLocation != null) {
                builder.latitude(initiatorLocation.getLocation().getLatitude().getValue())
                    .longitude(initiatorLocation.getLocation().getLongitude().getValue())
                    .accuracy(initiatorLocation.getLocation().getAccuracy() != null 
                        ? initiatorLocation.getLocation().getAccuracy() : null)
                    .locationUpdatedAt(initiatorLocation.getUpdatedAt() != null 
                        ? initiatorLocation.getUpdatedAt().format(DATE_TIME_FORMATTER) : null);
            }
            
            participantInfos.add(0, builder.build()); // Add at the beginning
        }

        // Return result
        return GetSessionDetailsResult.builder()
            .id(session.getId())
            .sessionId(session.getSessionId().getValue())
            .initiatorId(session.getInitiatorId())
            .initiatorUsername(initiator.getUsername().getValue())
            .status(session.getStatus().getValue())
            .createdAt(session.getCreatedAt().format(DATE_TIME_FORMATTER))
            .participants(participantInfos)
            .participantCount((long) participantInfos.size())
            .meetingLocationLatitude(session.getMeetingLocation() != null
                    ? session.getMeetingLocation().getLatitude().getValue() : null)
            .meetingLocationLongitude(session.getMeetingLocation() != null
                    ? session.getMeetingLocation().getLongitude().getValue() : null)
            .build();
    }
}

