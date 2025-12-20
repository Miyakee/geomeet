package com.geomeet.api.application.usecase.session;

import com.geomeet.api.application.result.EndSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.SessionId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for broadcasting session end notifications via WebSocket.
 * Orchestrates the session end broadcast flow.
 */
@Service
@AllArgsConstructor
public class BroadcastSessionEndUseCase {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;


    /**
     * Executes the broadcast session end use case.
     * Broadcasts the session end notification to all subscribers.
     * Includes meeting location if it exists.
     *
     * @param result the end session result
     */
    public void execute(EndSessionResult result) {
        // Get session to check for meeting location
        SessionId sessionIdVO = SessionId.fromString(result.getSessionIdString());
        Session session = sessionRepository.findBySessionId(sessionIdVO).orElse(null);

        // Build session end notification with meeting location if available
        SessionEndNotification notification = SessionEndNotification.builder()
            .sessionId(result.getSessionId())
            .sessionIdString(result.getSessionIdString())
            .status(result.getStatus())
            .endedAt(result.getEndedAt())
            .message(result.getMessage())
            .hasMeetingLocation(session != null && session.getMeetingLocation() != null)
            .meetingLocationLatitude(
                session != null && session.getMeetingLocation() != null
                    ? session.getMeetingLocation().getLatitude().getValue()
                    : null
            )
            .meetingLocationLongitude(
                session != null && session.getMeetingLocation() != null
                    ? session.getMeetingLocation().getLongitude().getValue()
                    : null
            )
            .build();

        // Broadcast to all subscribers of this session
        String destination = "/topic/session/" + result.getSessionIdString() + "/end";
        messagingTemplate.convertAndSend(destination, notification);
    }

    /**
     * Notification DTO for session end.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class SessionEndNotification {
        private final Long sessionId;
        private final String sessionIdString;
        private final String status;
        private final String endedAt;
        private final String message;
        private final Boolean hasMeetingLocation;
        private final Double meetingLocationLatitude;
        private final Double meetingLocationLongitude;
    }
}

