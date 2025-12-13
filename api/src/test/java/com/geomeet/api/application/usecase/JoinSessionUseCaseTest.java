package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.JoinSessionCommand;
import com.geomeet.api.application.result.JoinSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JoinSessionUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    private JoinSessionUseCase joinSessionUseCase;

    private Long userId;
    private String sessionIdString;
    private SessionId sessionId;
    private Session activeSession;
    private Long sessionDbId;

    @BeforeEach
    void setUp() {
        joinSessionUseCase = new JoinSessionUseCase(sessionRepository, sessionParticipantRepository);
        userId = 1L;
        sessionIdString = "test-session-id-123";
        sessionId = SessionId.fromString(sessionIdString);
        sessionDbId = 100L;
        activeSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            2L, // initiatorId
            SessionStatus.ACTIVE,
            null, // createdAt
            null, // updatedAt
            null, // createdBy
            null  // updatedBy
        );
    }

    @Test
    void shouldExecuteJoinSessionSuccessfully() {
        // Given
        JoinSessionCommand command = JoinSessionCommand.of(sessionIdString, userId);
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(false);

        SessionParticipant participant = SessionParticipant.create(sessionDbId, userId);
        SessionParticipant savedParticipant = SessionParticipant.reconstruct(
            1L, // participantId
            sessionDbId,
            userId,
            participant.getJoinedAt(),
            participant.getCreatedAt(),
            participant.getUpdatedAt(),
            null, // createdBy
            null  // updatedBy
        );
        when(sessionParticipantRepository.save(any(SessionParticipant.class)))
            .thenReturn(savedParticipant);

        // When
        JoinSessionResult result = joinSessionUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(savedParticipant.getId(), result.getParticipantId());
        assertEquals(sessionDbId, result.getSessionId());
        assertEquals(sessionIdString, result.getSessionIdString());
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getJoinedAt());
        assertEquals("Successfully joined the session", result.getMessage());

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository).existsBySessionIdAndUserId(sessionDbId, userId);
        verify(sessionParticipantRepository).save(any(SessionParticipant.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        JoinSessionCommand command = JoinSessionCommand.of(sessionIdString, userId);
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            joinSessionUseCase.execute(command);
        });

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository, never()).existsBySessionIdAndUserId(anyLong(), anyLong());
        verify(sessionParticipantRepository, never()).save(any(SessionParticipant.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionIsEnded() {
        // Given
        Session endedSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            2L, // initiatorId
            SessionStatus.ENDED,
            null, // createdAt
            null, // updatedAt
            null, // createdBy
            null  // updatedBy
        );
        JoinSessionCommand command = JoinSessionCommand.of(sessionIdString, userId);
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(endedSession));

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            joinSessionUseCase.execute(command);
        });

        assertEquals("Cannot join a session that has ended", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository, never()).existsBySessionIdAndUserId(anyLong(), anyLong());
        verify(sessionParticipantRepository, never()).save(any(SessionParticipant.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyJoined() {
        // Given
        JoinSessionCommand command = JoinSessionCommand.of(sessionIdString, userId);
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(true);

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            joinSessionUseCase.execute(command);
        });

        assertEquals("User has already joined this session", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository).existsBySessionIdAndUserId(sessionDbId, userId);
        verify(sessionParticipantRepository, never()).save(any(SessionParticipant.class));
    }
}

