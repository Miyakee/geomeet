package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.UpdateMeetingLocationCommand;
import com.geomeet.api.application.result.UpdateMeetingLocationResult;
import com.geomeet.api.application.usecase.location.UpdateMeetingLocationUseCase;
import com.geomeet.api.application.usecase.session.BroadcastMeetingLocationUseCase;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.DomainException;
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

@ExtendWith(MockitoExtension.class)
class UpdateMeetingLocationUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private BroadcastMeetingLocationUseCase broadcastMeetingLocationUseCase;

    private UpdateMeetingLocationUseCase updateMeetingLocationUseCase;

    private Long initiatorId;
    private Long sessionDbId;
    private String sessionIdString;
    private SessionId sessionId;
    private Session activeSession;
    private Double latitude;
    private Double longitude;

    @BeforeEach
    void setUp() {
        updateMeetingLocationUseCase = new UpdateMeetingLocationUseCase(
            sessionRepository,
            broadcastMeetingLocationUseCase
        );

        initiatorId = 1L;
        sessionDbId = 100L;
        sessionIdString = "test-session-id-123";
        sessionId = SessionId.fromString(sessionIdString);
        latitude = 1.3521;
        longitude = 103.8198;

        activeSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            initiatorId,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null,
            null
        );
    }

    @Test
    void shouldExecuteUpdateMeetingLocationSuccessfully() {
        // Given
        UpdateMeetingLocationCommand command = UpdateMeetingLocationCommand.of(
            sessionIdString, initiatorId, latitude, longitude
        );

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));
        
        Session savedSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            initiatorId,
            SessionStatus.ACTIVE,
            activeSession.getCreatedAt(),
            LocalDateTime.now(),
            null,
            null,
            Location.of(latitude, longitude)
        );
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

        // When
        UpdateMeetingLocationResult result = updateMeetingLocationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(sessionDbId, result.getSessionId());
        assertEquals(sessionIdString, result.getSessionIdString());
        assertEquals(latitude, result.getLatitude());
        assertEquals(longitude, result.getLongitude());
        assertEquals("Meeting location updated successfully", result.getMessage());

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(any(Session.class));
        verify(broadcastMeetingLocationUseCase).execute(any(UpdateMeetingLocationResult.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        UpdateMeetingLocationCommand command = UpdateMeetingLocationCommand.of(
            sessionIdString, initiatorId, latitude, longitude
        );

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            updateMeetingLocationUseCase.execute(command);
        });

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionIsEnded() {
        // Given
        Session endedSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            initiatorId,
            SessionStatus.ENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null,
            null
        );

        UpdateMeetingLocationCommand command = UpdateMeetingLocationCommand.of(
            sessionIdString, initiatorId, latitude, longitude
        );

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(endedSession));

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            updateMeetingLocationUseCase.execute(command);
        });

        assertEquals("Cannot update meeting location for an ended session", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotInitiator() {
        // Given
        Long nonInitiatorUserId = 999L;
        UpdateMeetingLocationCommand command = UpdateMeetingLocationCommand.of(
            sessionIdString, nonInitiatorUserId, latitude, longitude
        );

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updateMeetingLocationUseCase.execute(command);
        });

        assertEquals("Only the session initiator can update the meeting location", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
        verify(broadcastMeetingLocationUseCase, never()).execute(any(UpdateMeetingLocationResult.class));
    }
}

