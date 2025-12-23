package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.CalculateOptimalLocationCommand;
import com.geomeet.api.application.result.CalculateOptimalLocationResult;
import com.geomeet.api.application.usecase.location.CalculateOptimalLocationUseCase;
import com.geomeet.api.application.usecase.location.ParticipantLocationRepository;
import com.geomeet.api.application.usecase.session.BroadcastOptimalLocationUseCase;
import com.geomeet.api.application.usecase.session.SessionParticipantRepository;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculateOptimalLocationUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    @Mock
    private ParticipantLocationRepository participantLocationRepository;

    @Mock
    private BroadcastOptimalLocationUseCase broadcastOptimalLocationUseCase;

    private CalculateOptimalLocationUseCase calculateOptimalLocationUseCase;

    private Long userId;
    private Long sessionDbId;
    private String sessionIdString;
    private SessionId sessionId;
    private Session activeSession;

    @BeforeEach
    void setUp() {
        calculateOptimalLocationUseCase = new CalculateOptimalLocationUseCase(
            sessionRepository,
            sessionParticipantRepository,
            participantLocationRepository,
            broadcastOptimalLocationUseCase
        );

        userId = 1L;
        sessionDbId = 100L;
        sessionIdString = "test-session-id-123";
        sessionId = SessionId.fromString(sessionIdString);
        activeSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            userId,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
    }

    @Test
    void shouldCalculateOptimalLocationSuccessfully() {
        // Given
        CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(
            sessionIdString, userId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(true);

        // Create participant locations
        Location location1 = Location.of(1.2903, 103.8520); // Marina Bay
        Location location2 = Location.of(1.2966, 103.7764); // Jurong East
        Location location3 = Location.of(1.3521, 103.8198); // Central

        ParticipantLocation participantLocation1 = ParticipantLocation.reconstruct(
            1L, 1L, sessionDbId, 1L,
            1.2903, 103.8520, null,
            LocalDateTime.now(), LocalDateTime.now(), null, null
        );
        ParticipantLocation participantLocation2 = ParticipantLocation.reconstruct(
            2L, 2L, sessionDbId, 2L,
            1.2966, 103.7764, null,
            LocalDateTime.now(), LocalDateTime.now(), null, null
        );
        ParticipantLocation participantLocation3 = ParticipantLocation.reconstruct(
            3L, 3L, sessionDbId, 3L,
            1.3521, 103.8198, null,
            LocalDateTime.now(), LocalDateTime.now(), null, null
        );

        List<ParticipantLocation> participantLocations = Arrays.asList(
            participantLocation1, participantLocation2, participantLocation3
        );

        when(participantLocationRepository.findBySessionId(sessionDbId))
            .thenReturn(participantLocations);

        // When
        CalculateOptimalLocationResult result = calculateOptimalLocationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(sessionDbId, result.getSessionId());
        assertEquals(sessionIdString, result.getSessionIdString());
        assertNotNull(result.getOptimalLatitude());
        assertNotNull(result.getOptimalLongitude());
        assertEquals(3, result.getParticipantCount());
        assertNotNull(result.getTotalTravelDistance());
        assertTrue(result.getTotalTravelDistance() > 0);

        // Verify geometric center calculation (average of three locations)
        double expectedLat = (1.2903 + 1.2966 + 1.3521) / 3.0;
        double expectedLon = (103.8520 + 103.7764 + 103.8198) / 3.0;
        assertEquals(expectedLat, result.getOptimalLatitude(), 0.0001);
        assertEquals(expectedLon, result.getOptimalLongitude(), 0.0001);

        // Verify broadcast was called
        verify(broadcastOptimalLocationUseCase).execute(any(CalculateOptimalLocationResult.class));
    }

    @Test
    void shouldCalculateOptimalLocationForSingleParticipant() {
        // Given
        CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(
            sessionIdString, userId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(true);

        ParticipantLocation participantLocation = ParticipantLocation.reconstruct(
            1L, 1L, sessionDbId, 1L,
            1.2903, 103.8520, null,
            LocalDateTime.now(), LocalDateTime.now(), null, null
        );

        when(participantLocationRepository.findBySessionId(sessionDbId))
            .thenReturn(Collections.singletonList(participantLocation));

        // When
        CalculateOptimalLocationResult result = calculateOptimalLocationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(1.2903, result.getOptimalLatitude(), 0.0001);
        assertEquals(103.8520, result.getOptimalLongitude(), 0.0001);
        assertEquals(1, result.getParticipantCount());
        assertEquals(0.0, result.getTotalTravelDistance(), 0.001); // Distance to self is 0
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(
            sessionIdString, userId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(GeomeetDomainException.class, () -> {
            calculateOptimalLocationUseCase.execute(command);
        });

        verify(participantLocationRepository, never()).findBySessionId(anyLong());
        verify(broadcastOptimalLocationUseCase, never()).execute(any());
    }

    @Test
    void shouldThrowExceptionWhenSessionIsEnded() {
        // Given
        Session endedSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            userId,
            SessionStatus.ENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(
            sessionIdString, userId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(endedSession));

        // When & Then
        assertThrows(GeomeetDomainException.class, () -> {
            calculateOptimalLocationUseCase.execute(command);
        });

        verify(participantLocationRepository, never()).findBySessionId(anyLong());
        verify(broadcastOptimalLocationUseCase, never()).execute(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotParticipant() {
        // Given - User is not participant and not initiator
        Long differentUserId = 999L;
        Session sessionWithDifferentInitiator = Session.reconstruct(
            sessionDbId,
            sessionId,
            888L, // Different initiator
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(
            sessionIdString, differentUserId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(sessionWithDifferentInitiator));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionDbId, differentUserId))
            .thenReturn(false);

        // When & Then
        assertThrows(GeomeetDomainException.class, () -> {
            calculateOptimalLocationUseCase.execute(command);
        });

        verify(participantLocationRepository, never()).findBySessionId(anyLong());
        verify(broadcastOptimalLocationUseCase, never()).execute(any());
    }

    @Test
    void shouldAllowInitiatorToCalculateOptimalLocation() {
        // Given
        CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(
            sessionIdString, userId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(false); // Not a participant, but is initiator

        ParticipantLocation participantLocation = ParticipantLocation.reconstruct(
            1L, 1L, sessionDbId, 1L,
            1.2903, 103.8520, null,
            LocalDateTime.now(), LocalDateTime.now(), null, null
        );

        when(participantLocationRepository.findBySessionId(sessionDbId))
            .thenReturn(Collections.singletonList(participantLocation));

        // When
        CalculateOptimalLocationResult result = calculateOptimalLocationUseCase.execute(command);

        // Then
        assertNotNull(result);
        verify(broadcastOptimalLocationUseCase).execute(any(CalculateOptimalLocationResult.class));
    }

    @Test
    void shouldThrowExceptionWhenNoParticipantLocations() {
        // Given
        CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(
            sessionIdString, userId);

        when(sessionRepository.findBySessionId(any(SessionId.class)))
            .thenReturn(Optional.of(activeSession));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionDbId, userId))
            .thenReturn(true);
        when(participantLocationRepository.findBySessionId(sessionDbId))
            .thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(GeomeetDomainException.class, () -> {
            calculateOptimalLocationUseCase.execute(command);
        });

        verify(broadcastOptimalLocationUseCase, never()).execute(any());
    }
}

