package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.result.GetSessionDetailsResult;
import com.geomeet.api.application.usecase.auth.UserRepository;
import com.geomeet.api.application.usecase.location.ParticipantLocationRepository;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.valueobject.SessionId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for broadcasting session updates via WebSocket.
 * Orchestrates the session update broadcast flow.
 */
@Service
@AllArgsConstructor
public class BroadcastSessionUpdateUseCase {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final UserRepository userRepository;
    private final ParticipantLocationRepository participantLocationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Executes the broadcast session update use case.
     * Retrieves the latest session details and broadcasts to all subscribers.
     *
     * @param sessionIdString the session ID string
     */
    public void execute(String sessionIdString) {
        SessionId sessionIdVO = SessionId.fromString(sessionIdString);
        Session session = sessionRepository.findBySessionId(sessionIdVO)
            .orElse(null);

        if (session == null) {
            return;
        }

        // Get initiator info
        User initiator = userRepository.findById(session.getInitiatorId())
            .orElse(null);

        if (initiator == null) {
            return;
        }

        // Get all participant locations for this session (create a map for quick lookup)
        // This includes locations for all users who have shared their location, even if they disconnected
        List<ParticipantLocation> participantLocations = participantLocationRepository.findBySessionId(session.getId());
        Map<Long, ParticipantLocation> locationMap = participantLocations.stream()
            .collect(Collectors.toMap(
                ParticipantLocation::getUserId,
                location -> location,
                (existing, replacement) -> existing // Keep first if duplicate
            ));

        // Get all participants (those who have joined the session)
        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionId(session.getId());
        
        // Build participant info list from actual participants
        List<GetSessionDetailsResult.ParticipantInfo> participantInfos = participants.stream()
            .map(participant -> {
                User user = userRepository.findById(participant.getUserId())
                    .orElse(null);
                if (user == null) {
                    return null;
                }
                
                // Get location for this participant if available
                ParticipantLocation location = locationMap.get(participant.getUserId());
                
                GetSessionDetailsResult.ParticipantInfo.ParticipantInfoBuilder builder =
                    GetSessionDetailsResult.ParticipantInfo.builder()
                    .participantId(participant.getId())
                    .userId(participant.getUserId())
                    .username(user.getUsername().getValue())
                    .email(user.getEmail().getValue())
                    .joinedAt(participant.getJoinedAt().format(DATE_TIME_FORMATTER));
                
                // Add location information if available (preserves last known location even after disconnect)
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
            .filter(java.util.Objects::nonNull)
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
            
            // Add location information for initiator if available
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
        
        // IMPORTANT: Also include users who have shared location but are no longer in participants list
        // This ensures we preserve location info for disconnected users
        for (Map.Entry<Long, ParticipantLocation> entry : locationMap.entrySet()) {
            Long userId = entry.getKey();
            ParticipantLocation location = entry.getValue();
            
            // Skip if already in participantInfos or if it's the initiator (already handled above)
            if (userId.equals(session.getInitiatorId()) || 
                participantInfos.stream().anyMatch(p -> p.getUserId().equals(userId))) {
                continue;
            }
            
            // This user has location info but is not in the participants list (likely disconnected)
            // Still include them with their last known location
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                GetSessionDetailsResult.ParticipantInfo disconnectedParticipant =
                    GetSessionDetailsResult.ParticipantInfo.builder()
                    .participantId(null) // No participant record
                    .userId(userId)
                    .username(user.getUsername().getValue())
                    .email(user.getEmail().getValue())
                    .joinedAt(null) // Unknown join time for disconnected users
                    .latitude(location.getLocation().getLatitude().getValue())
                    .longitude(location.getLocation().getLongitude().getValue())
                    .accuracy(location.getLocation().getAccuracy() != null 
                        ? location.getLocation().getAccuracy() : null)
                    .locationUpdatedAt(location.getUpdatedAt() != null 
                        ? location.getUpdatedAt().format(DATE_TIME_FORMATTER) : null)
                    .build();
                participantInfos.add(disconnectedParticipant);
            }
        }

        // Build result
        GetSessionDetailsResult result = GetSessionDetailsResult.builder()
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

        // Broadcast to all subscribers of this session
        messagingTemplate.convertAndSend("/topic/session/" + sessionIdString, result);
    }
}

