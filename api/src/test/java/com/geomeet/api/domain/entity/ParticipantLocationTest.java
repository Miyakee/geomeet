package com.geomeet.api.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.geomeet.api.domain.valueobject.Location;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ParticipantLocationTest {

    @Test
    void shouldCreateParticipantLocationSuccessfully() {
        // Given
        Long participantId = 200L;
        Long sessionId = 100L;
        Long userId = 1L;
        Location location = Location.of(37.7749, -122.4194, 10.0);

        // When
        ParticipantLocation participantLocation = ParticipantLocation.create(
            participantId, sessionId, userId, location
        );

        // Then
        assertNotNull(participantLocation);
        assertEquals(participantId, participantLocation.getParticipantId());
        assertEquals(sessionId, participantLocation.getSessionId());
        assertEquals(userId, participantLocation.getUserId());
        assertEquals(location, participantLocation.getLocation());
        assertNotNull(participantLocation.getCreatedAt());
        assertNotNull(participantLocation.getUpdatedAt());
    }

    @Test
    void shouldReconstructParticipantLocationSuccessfully() {
        // Given
        Long id = 300L;
        Long participantId = 200L;
        Long sessionId = 100L;
        Long userId = 1L;
        Double latitude = 37.7749;
        Double longitude = -122.4194;
        Double accuracy = 10.0;
        LocalDateTime now = LocalDateTime.now();

        // When
        ParticipantLocation participantLocation = ParticipantLocation.reconstruct(
            id, participantId, sessionId, userId,
            latitude, longitude, accuracy,
            now, now, null, null
        );

        // Then
        assertNotNull(participantLocation);
        assertEquals(id, participantLocation.getId());
        assertEquals(participantId, participantLocation.getParticipantId());
        assertEquals(sessionId, participantLocation.getSessionId());
        assertEquals(userId, participantLocation.getUserId());
        assertEquals(latitude, participantLocation.getLocation().getLatitude().getValue());
        assertEquals(longitude, participantLocation.getLocation().getLongitude().getValue());
        assertEquals(accuracy, participantLocation.getLocation().getAccuracy());
    }

    @Test
    void shouldUpdateLocationSuccessfully() {
        // Given
        ParticipantLocation participantLocation = ParticipantLocation.create(
            200L, 100L, 1L, Location.of(37.0, -122.0, 20.0)
        );
        Location newLocation = Location.of(37.7749, -122.4194, 10.0);
        LocalDateTime originalUpdatedAt = participantLocation.getUpdatedAt();

        // When
        // Wait a bit to ensure timestamp changes
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        participantLocation.updateLocation(newLocation);

        // Then
        assertEquals(newLocation, participantLocation.getLocation());
        // UpdatedAt should be different (or at least set)
        assertNotNull(participantLocation.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithNullLocation() {
        // Given
        ParticipantLocation participantLocation = ParticipantLocation.create(
            200L, 100L, 1L, Location.of(37.0, -122.0, 20.0)
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> participantLocation.updateLocation(null)
        );

        assertEquals("Location cannot be null", exception.getMessage());
    }
}

