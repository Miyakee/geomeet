package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.EndSessionCommand;
import com.geomeet.api.application.result.EndSessionResult;
import com.geomeet.api.application.usecase.session.BroadcastSessionEndUseCase;
import com.geomeet.api.application.usecase.session.EndSessionUseCase;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EndSessionUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private BroadcastSessionEndUseCase broadcastSessionEndUseCase;

    private EndSessionUseCase endSessionUseCase;

    private Long initiatorId;
    private Long differentUserId;
    private Long sessionDbId;
    private String sessionIdString;
    private SessionId sessionId;
    private Session activeSession;

    @BeforeEach
    void setUp() {
        endSessionUseCase = new EndSessionUseCase(sessionRepository, broadcastSessionEndUseCase);

        initiatorId = 1L;
        differentUserId = 2L;
        sessionDbId = 100L;
        sessionIdString = "test-session-id-123";
        sessionId = SessionId.fromString(sessionIdString);
        activeSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            initiatorId,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
    }

    @Test
    void shouldEndSessionSuccessfully() {
        // Given
        EndSessionCommand command = EndSessionCommand.of(sessionIdString, initiatorId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(activeSession));
        when(sessionRepository.save(any(Session.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EndSessionResult result = endSessionUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(sessionDbId, result.getSessionId());
        assertEquals(sessionIdString, result.getSessionIdString());
        assertEquals("Ended", result.getStatus());
        assertEquals("Session ended successfully", result.getMessage());
        assertNotNull(result.getEndedAt());

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(any(Session.class));
        verify(broadcastSessionEndUseCase).execute(any(EndSessionResult.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        EndSessionCommand command = EndSessionCommand.of(sessionIdString, initiatorId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> {
            endSessionUseCase.execute(command);
        });

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotInitiator() {
        // Given
        EndSessionCommand command = EndSessionCommand.of(sessionIdString, differentUserId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(activeSession));

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            endSessionUseCase.execute(command);
        });

        assertEquals("Only the session initiator can end the session", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionAlreadyEnded() {
        // Given
        Session endedSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            initiatorId,
            SessionStatus.ENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        EndSessionCommand command = EndSessionCommand.of(sessionIdString, initiatorId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(endedSession));

        // When & Then
        // The session.end() method will throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            endSessionUseCase.execute(command);
        });

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
        verify(broadcastSessionEndUseCase, never()).execute(any(EndSessionResult.class));
    }
}

