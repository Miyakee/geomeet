package com.geomeet.api.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import com.geomeet.api.domain.valueobject.Username;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BroadcastSessionUpdateUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private BroadcastSessionUpdateUseCase broadcastSessionUpdateUseCase;

    private Long sessionId;
    private String sessionIdString;
    private Long initiatorId;
    private Long userId;
    private Session session;
    private User initiator;
    private User participantUser;
    private SessionParticipant participant;

    @BeforeEach
    void setUp() {
        broadcastSessionUpdateUseCase = new BroadcastSessionUpdateUseCase(
            sessionRepository,
            sessionParticipantRepository,
            userRepository,
            messagingTemplate
        );

        sessionId = 100L;
        sessionIdString = "test-session-id-123";
        initiatorId = 1L;
        userId = 2L;

        SessionId sessionIdVO = SessionId.fromString(sessionIdString);
        session = Session.reconstruct(
            sessionId,
            sessionIdVO,
            initiatorId,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        initiator = User.reconstruct(
            initiatorId,
            new Username("initiator"),
            new Email("initiator@example.com"),
            new PasswordHash("hash"),
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        participantUser = User.reconstruct(
            userId,
            new Username("participant"),
            new Email("participant@example.com"),
            new PasswordHash("hash"),
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        participant = SessionParticipant.reconstruct(
            200L,
            sessionId,
            userId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
    }

    @Test
    void shouldExecuteBroadcastSessionUpdateSuccessfully() {
        // Given
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId)).thenReturn(List.of(participant));
        when(userRepository.findById(userId)).thenReturn(Optional.of(participantUser));

        // When
        broadcastSessionUpdateUseCase.execute(sessionIdString);

        // Then
        verify(sessionRepository).findBySessionId(any(SessionId.class));
        verify(userRepository).findById(initiatorId);
        verify(sessionParticipantRepository).findBySessionId(sessionId);
        verify(userRepository).findById(userId);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/session/" + sessionIdString),
            any(com.geomeet.api.application.result.GetSessionDetailsResult.class)
        );
    }

    @Test
    void shouldNotBroadcastWhenSessionNotFound() {
        // Given
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.empty());

        // When
        broadcastSessionUpdateUseCase.execute(sessionIdString);

        // Then
        verify(sessionRepository).findBySessionId(any(SessionId.class));
        // Should not call messagingTemplate when session not found
        verify(messagingTemplate, never()).convertAndSend(
            eq("/topic/session/" + sessionIdString),
            any(Object.class)
        );
    }

    @Test
    void shouldNotBroadcastWhenInitiatorNotFound() {
        // Given
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.empty());

        // When
        broadcastSessionUpdateUseCase.execute(sessionIdString);

        // Then
        verify(sessionRepository).findBySessionId(any(SessionId.class));
        verify(userRepository).findById(initiatorId);
        // Should not call messagingTemplate when initiator not found
        verify(messagingTemplate, never()).convertAndSend(
            eq("/topic/session/" + sessionIdString),
            any(Object.class)
        );
    }

    @Test
    void shouldHandleNullParticipantUser() {
        // Given
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId)).thenReturn(List.of(participant));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        broadcastSessionUpdateUseCase.execute(sessionIdString);

        // Then
        verify(sessionRepository).findBySessionId(any(SessionId.class));
        verify(userRepository).findById(initiatorId);
        verify(sessionParticipantRepository).findBySessionId(sessionId);
        verify(userRepository).findById(userId);
        // Should still broadcast even if some participant users are not found (filtered out)
        verify(messagingTemplate).convertAndSend(
            eq("/topic/session/" + sessionIdString),
            any(com.geomeet.api.application.result.GetSessionDetailsResult.class)
        );
    }

    @Test
    void shouldNotAddInitiatorWhenAlreadyInParticipants() {
        // Given - initiator is already a participant
        SessionParticipant initiatorParticipant = SessionParticipant.reconstruct(
            201L,
            sessionId,
            initiatorId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId))
            .thenReturn(List.of(initiatorParticipant, participant));
        when(userRepository.findById(userId)).thenReturn(Optional.of(participantUser));

        // When
        broadcastSessionUpdateUseCase.execute(sessionIdString);

        // Then
        verify(sessionRepository).findBySessionId(any(SessionId.class));
        verify(userRepository, times(2)).findById(initiatorId); // Called once for initiator, once in participant stream
        verify(sessionParticipantRepository).findBySessionId(sessionId);
        verify(userRepository).findById(userId);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/session/" + sessionIdString),
            any(com.geomeet.api.application.result.GetSessionDetailsResult.class)
        );
    }
}

