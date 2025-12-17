package com.geomeet.api.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.result.EndSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BroadcastSessionEndUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private BroadcastSessionEndUseCase broadcastSessionEndUseCase;

    private Long sessionDbId;
    private String sessionIdString;
    private SessionId sessionId;
    private EndSessionResult endSessionResult;

    @BeforeEach
    void setUp() {
        broadcastSessionEndUseCase = new BroadcastSessionEndUseCase(sessionRepository, messagingTemplate);

        sessionDbId = 100L;
        sessionIdString = "test-session-id-123";
        sessionId = SessionId.fromString(sessionIdString);
        endSessionResult = EndSessionResult.builder()
            .sessionId(sessionDbId)
            .sessionIdString(sessionIdString)
            .status("Ended")
            .endedAt("2024-01-01T12:00:00")
            .message("Session ended successfully")
            .build();
    }

    @Test
    void shouldBroadcastSessionEndWithMeetingLocation() {
        // Given
        Location meetingLocation = Location.of(1.3521, 103.8198);
        Session session = Session.reconstruct(
            sessionDbId,
            sessionId,
            1L,
            SessionStatus.ENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null,
            meetingLocation
        );

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(session));

        // When
        broadcastSessionEndUseCase.execute(endSessionResult);

        // Then
        String expectedDestination = "/topic/session/" + sessionIdString + "/end";
        verify(messagingTemplate).convertAndSend(
                eq(expectedDestination), any(BroadcastSessionEndUseCase.SessionEndNotification.class));
    }

    @Test
    void shouldBroadcastSessionEndWithoutMeetingLocation() {
        // Given
        Session session = Session.reconstruct(
            sessionDbId,
            sessionId,
            1L,
            SessionStatus.ENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null,
            null // No meeting location
        );

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(session));

        // When
        broadcastSessionEndUseCase.execute(endSessionResult);

        // Then
        String expectedDestination = "/topic/session/" + sessionIdString + "/end";
        verify(messagingTemplate).convertAndSend(
                eq(expectedDestination), any(BroadcastSessionEndUseCase.SessionEndNotification.class));
    }

    @Test
    void shouldHandleSessionNotFound() {
        // Given
        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.empty());

        // When
        broadcastSessionEndUseCase.execute(endSessionResult);

        // Then
        String expectedDestination = "/topic/session/" + sessionIdString + "/end";
        verify(messagingTemplate).convertAndSend(
                eq(expectedDestination), any(BroadcastSessionEndUseCase.SessionEndNotification.class));
    }
}

