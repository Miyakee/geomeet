package com.geomeet.api.application.usecase;

import com.geomeet.api.application.result.GetSessionDetailsResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.valueobject.SessionId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for broadcasting session updates via WebSocket.
 * Orchestrates the session update broadcast flow.
 */
@Service
public class BroadcastSessionUpdateUseCase {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BroadcastSessionUpdateUseCase(
        SessionRepository sessionRepository,
        SessionParticipantRepository sessionParticipantRepository,
        UserRepository userRepository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.sessionRepository = sessionRepository;
        this.sessionParticipantRepository = sessionParticipantRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

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

        // Get all participants
        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionId(session.getId());
        List<GetSessionDetailsResult.ParticipantInfo> participantInfos = participants.stream()
            .map(participant -> {
                User user = userRepository.findById(participant.getUserId())
                    .orElse(null);
                if (user == null) {
                    return null;
                }
                return GetSessionDetailsResult.ParticipantInfo.builder()
                    .participantId(participant.getId())
                    .userId(participant.getUserId())
                    .username(user.getUsername().getValue())
                    .email(user.getEmail().getValue())
                    .joinedAt(participant.getJoinedAt().format(DATE_TIME_FORMATTER))
                    .build();
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

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
            .build();

        // Broadcast to all subscribers of this session
        messagingTemplate.convertAndSend("/topic/session/" + sessionIdString, result);
    }
}

