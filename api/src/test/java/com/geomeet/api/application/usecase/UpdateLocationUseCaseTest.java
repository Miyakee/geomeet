package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.UpdateLocationCommand;
import com.geomeet.api.application.result.UpdateLocationResult;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
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
class UpdateLocationUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    @Mock
    private ParticipantLocationRepository participantLocationRepository;

    private UpdateLocationUseCase updateLocationUseCase;

    private Long userId;
    private Long sessionDbId;
    private String sessionIdString;
    private SessionId sessionId;
    private Session activeSession;
    private SessionParticipant participant;
    private Double latitude;
    private Double longitude;
    private Double accuracy;

    @BeforeEach
    void setUp() {
        updateLocationUseCase = new UpdateLocationUseCase(
            sessionRepository,
            sessionParticipantRepository,
            participantLocationRepository
        );

        userId = 1L;
        sessionDbId = 100L;
        sessionIdString = "test-session-id-123";
        sessionId = SessionId.fromString(sessionIdString);
        latitude = 37.7749;
        longitude = -122.4194;
        accuracy = 10.0;

        activeSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            2L, // initiatorId
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        participant = SessionParticipant.reconstruct(
            200L, // participantId
            sessionDbId,
            userId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
    }

    @Test
    void shouldExecuteUpdateLocationSuccessfullyWhenLocationDoesNotExist() {
        // Given
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionIdString, userId, latitude, longitude, accuracy
        );
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.findBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(Optional.of(participant));
        when(participantLocationRepository.findByParticipantId(participant.getId()))
            .thenReturn(Optional.empty());

        ParticipantLocation newLocation = ParticipantLocation.create(
            participant.getId(),
            sessionDbId,
            userId,
            Location.of(latitude, longitude, accuracy)
        );
        ParticipantLocation savedLocation = ParticipantLocation.reconstruct(
            300L, // locationId
            participant.getId(),
            sessionDbId,
            userId,
            latitude,
            longitude,
            accuracy,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
        when(participantLocationRepository.save(any(ParticipantLocation.class)))
            .thenReturn(savedLocation);

        // When
        UpdateLocationResult result = updateLocationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(participant.getId(), result.getParticipantId());
        assertEquals(sessionDbId, result.getSessionId());
        assertEquals(sessionIdString, result.getSessionIdString());
        assertEquals(userId, result.getUserId());
        assertEquals(latitude, result.getLatitude());
        assertEquals(longitude, result.getLongitude());
        assertEquals(accuracy, result.getAccuracy());
        assertNotNull(result.getUpdatedAt());
        assertEquals("Location updated successfully", result.getMessage());

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository).findBySessionIdAndUserId(sessionDbId, userId);
        verify(participantLocationRepository).findByParticipantId(participant.getId());
        verify(participantLocationRepository).save(any(ParticipantLocation.class));
    }

    @Test
    void shouldExecuteUpdateLocationSuccessfullyWhenLocationExists() {
        // Given
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionIdString, userId, latitude, longitude, accuracy
        );
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.findBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(Optional.of(participant));

        ParticipantLocation existingLocation = ParticipantLocation.reconstruct(
            300L, // locationId
            participant.getId(),
            sessionDbId,
            userId,
            37.0, // old latitude
            -122.0, // old longitude
            20.0, // old accuracy
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusHours(1),
            null,
            null
        );
        when(participantLocationRepository.findByParticipantId(participant.getId()))
            .thenReturn(Optional.of(existingLocation));

        ParticipantLocation updatedLocation = ParticipantLocation.reconstruct(
            300L, // locationId
            participant.getId(),
            sessionDbId,
            userId,
            latitude,
            longitude,
            accuracy,
            LocalDateTime.now(),
            existingLocation.getCreatedAt(),
            null,
            null
        );
        when(participantLocationRepository.save(any(ParticipantLocation.class)))
            .thenReturn(updatedLocation);

        // When
        UpdateLocationResult result = updateLocationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(latitude, result.getLatitude());
        assertEquals(longitude, result.getLongitude());
        assertEquals(accuracy, result.getAccuracy());

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository).findBySessionIdAndUserId(sessionDbId, userId);
        verify(participantLocationRepository).findByParticipantId(participant.getId());
        verify(participantLocationRepository).save(any(ParticipantLocation.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionIdString, userId, latitude, longitude, accuracy
        );
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            updateLocationUseCase.execute(command);
        });

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository, never()).findBySessionIdAndUserId(anyLong(), anyLong());
        verify(participantLocationRepository, never()).save(any(ParticipantLocation.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionIsEnded() {
        // Given
        Session endedSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            2L, // initiatorId
            SessionStatus.ENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionIdString, userId, latitude, longitude, accuracy
        );
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(endedSession));

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            updateLocationUseCase.execute(command);
        });

        assertEquals("Cannot update location for an ended session", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository, never()).findBySessionIdAndUserId(anyLong(), anyLong());
        verify(participantLocationRepository, never()).save(any(ParticipantLocation.class));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotParticipant() {
        // Given
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionIdString, userId, latitude, longitude, accuracy
        );
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.findBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            updateLocationUseCase.execute(command);
        });

        assertEquals("User is not a participant in this session", exception.getMessage());
        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionParticipantRepository).findBySessionIdAndUserId(sessionDbId, userId);
        verify(participantLocationRepository, never()).save(any(ParticipantLocation.class));
    }

    @Test
    void shouldExecuteUpdateLocationWithoutAccuracy() {
        // Given
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionIdString, userId, latitude, longitude
        );
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.findBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(Optional.of(participant));
        when(participantLocationRepository.findByParticipantId(participant.getId()))
            .thenReturn(Optional.empty());

        ParticipantLocation savedLocation = ParticipantLocation.reconstruct(
            300L,
            participant.getId(),
            sessionDbId,
            userId,
            latitude,
            longitude,
            null, // no accuracy
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
        when(participantLocationRepository.save(any(ParticipantLocation.class)))
            .thenReturn(savedLocation);

        // When
        UpdateLocationResult result = updateLocationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(latitude, result.getLatitude());
        assertEquals(longitude, result.getLongitude());
        assertEquals(null, result.getAccuracy());
    }
}

