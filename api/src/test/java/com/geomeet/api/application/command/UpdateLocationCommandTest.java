package com.geomeet.api.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UpdateLocationCommandTest {

    @Test
    void shouldCreateUpdateLocationCommandSuccessfully() {
        // Given
        String sessionId = "test-session-id";
        Long userId = 1L;
        Double latitude = 37.7749;
        Double longitude = -122.4194;
        Double accuracy = 10.0;

        // When
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionId, userId, latitude, longitude, accuracy
        );

        // Then
        assertNotNull(command);
        assertEquals(sessionId, command.getSessionId());
        assertEquals(userId, command.getUserId());
        assertEquals(latitude, command.getLatitude());
        assertEquals(longitude, command.getLongitude());
        assertEquals(accuracy, command.getAccuracy());
    }

    @Test
    void shouldCreateUpdateLocationCommandWithoutAccuracy() {
        // Given
        String sessionId = "test-session-id";
        Long userId = 1L;
        Double latitude = 37.7749;
        Double longitude = -122.4194;

        // When
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionId, userId, latitude, longitude
        );

        // Then
        assertNotNull(command);
        assertEquals(sessionId, command.getSessionId());
        assertEquals(userId, command.getUserId());
        assertEquals(latitude, command.getLatitude());
        assertEquals(longitude, command.getLongitude());
        assertEquals(null, command.getAccuracy());
    }

    @Test
    void shouldCreateUpdateLocationCommandUsingBuilder() {
        // Given
        String sessionId = "test-session-id";
        Long userId = 1L;
        Double latitude = 37.7749;
        Double longitude = -122.4194;
        Double accuracy = 10.0;

        // When
        UpdateLocationCommand command = UpdateLocationCommand.builder()
            .sessionId(sessionId)
            .userId(userId)
            .latitude(latitude)
            .longitude(longitude)
            .accuracy(accuracy)
            .build();

        // Then
        assertNotNull(command);
        assertEquals(sessionId, command.getSessionId());
        assertEquals(userId, command.getUserId());
        assertEquals(latitude, command.getLatitude());
        assertEquals(longitude, command.getLongitude());
        assertEquals(accuracy, command.getAccuracy());
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UpdateLocationCommand(null, 1L, 37.0, -122.0, 10.0)
        );

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UpdateLocationCommand("   ", 1L, 37.0, -122.0, 10.0)
        );

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UpdateLocationCommand("test-session-id", null, 37.0, -122.0, 10.0)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UpdateLocationCommand("test-session-id", 1L, null, -122.0, 10.0)
        );

        assertEquals("Latitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UpdateLocationCommand("test-session-id", 1L, 37.0, null, 10.0)
        );

        assertEquals("Longitude cannot be null", exception.getMessage());
    }
}

