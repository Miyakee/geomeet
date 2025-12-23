package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.command.GetSessionDetailsCommand;
import com.geomeet.api.application.result.GetSessionDetailsResult;
import com.geomeet.api.application.usecase.auth.UserRepository;
import com.geomeet.api.application.usecase.location.ParticipantLocationRepository;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.exception.DomainException;
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
     * @throws DomainException if session not found, user not found, or access denied
     */
    public GetSessionDetailsResult execute(GetSessionDetailsCommand command) {
        // Find session by sessionId
        SessionId sessionIdVO = SessionId.fromString(command.getSessionId());
        Optional<Session> sessionOpt = sessionRepository.findBySessionId(sessionIdVO);
        
        // Security: If session doesn't exist, return "Access denied" instead of "Session not found"
        // to prevent information disclosure through enumeration attacks
        if (sessionOpt.isEmpty()) {
            throw new DomainException("Access denied: User is not a participant or initiator");
        }
        
        Session session = sessionOpt.get();

        // Check if user is a participant or initiator
        boolean isParticipant = sessionParticipantRepository.existsBySessionIdAndUserId(
            session.getId(), command.getUserId()
        );
        if (!isParticipant && !session.getInitiatorId().equals(command.getUserId())) {
            throw new DomainException("Access denied: User is not a participant or initiator");
        }

        // Get initiator info
        User initiator = userRepository.findById(session.getInitiatorId())
            .orElseThrow(() -> new DomainException("Initiator not found"));

        // Get all participants
        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionId(session.getId());
        List<GetSessionDetailsResult.ParticipantInfo> participantInfos = participants.stream()
            .map(participant -> {
                User user = userRepository.findById(participant.getUserId())
                    .orElseThrow(() -> new DomainException("User not found for participant"));
                return GetSessionDetailsResult.ParticipantInfo.builder()
                    .participantId(participant.getId())
                    .userId(participant.getUserId())
                    .username(user.getUsername().getValue())
                    .email(user.getEmail().getValue())
                    .joinedAt(participant.getJoinedAt().format(DATE_TIME_FORMATTER))
                    .build();
            })
            .collect(Collectors.toList());

        // Check if initiator is in the participants list, if not, add them
        boolean initiatorInParticipants = participantInfos.stream()
            .anyMatch(p -> p.getUserId().equals(session.getInitiatorId()));
        
        if (!initiatorInParticipants) {
            // Add initiator as a participant
            GetSessionDetailsResult.ParticipantInfo initiatorInfo = GetSessionDetailsResult.ParticipantInfo.builder()
                .participantId(null) // Initiator doesn't have a participant ID
                .userId(session.getInitiatorId())
                .username(initiator.getUsername().getValue())
                .email(initiator.getEmail().getValue())
                .joinedAt(session.getCreatedAt().format(DATE_TIME_FORMATTER))
                .build();
            participantInfos.add(0, initiatorInfo); // Add at the beginning
        }

        // Get all participant locations for this session
        List<ParticipantLocation> participantLocations = participantLocationRepository.findBySessionId(session.getId());
        List<GetSessionDetailsResult.ParticipantLocationInfo> participantLocationInfos = participantLocations.stream()
            .map(location -> GetSessionDetailsResult.ParticipantLocationInfo.builder()
                .participantId(location.getParticipantId())
                .userId(location.getUserId())
                .latitude(location.getLocation().getLatitude().getValue())
                .longitude(location.getLocation().getLongitude().getValue())
                .accuracy(location.getLocation().getAccuracy() != null 
                    ? location.getLocation().getAccuracy() : null)
                .updatedAt(location.getUpdatedAt() != null 
                    ? location.getUpdatedAt().format(DATE_TIME_FORMATTER) : null)
                .build())
            .collect(Collectors.toList());

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
            .participantLocations(participantLocationInfos)
            .build();
    }
}

