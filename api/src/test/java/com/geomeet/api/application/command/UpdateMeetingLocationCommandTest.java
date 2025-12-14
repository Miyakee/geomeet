package com.geomeet.api.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UpdateMeetingLocationCommandTest {

    @Test
    void shouldCreateUpdateMeetingLocationCommandSuccessfully() {
        // Given
        String sessionId = "test-session-id";
        Long userId = 1L;
        Double latitude = 1.3521;
        Double longitude = 103.8198;

        // When
        UpdateMeetingLocationCommand command = UpdateMeetingLocationCommand.of(
            sessionId, userId, latitude, longitude
        );

        // Then
        assertNotNull(command);
        assertEquals(sessionId, command.getSessionId());
        assertEquals(userId, command.getUserId());
        assertEquals(latitude, command.getLatitude());
        assertEquals(longitude, command.getLongitude());
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateMeetingLocationCommand.of(null, 1L, 1.3521, 103.8198)
        );

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateMeetingLocationCommand.of("   ", 1L, 1.3521, 103.8198)
        );

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateMeetingLocationCommand.of("test-session-id", null, 1.3521, 103.8198)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateMeetingLocationCommand.of("test-session-id", 1L, null, 103.8198)
        );

        assertEquals("Latitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateMeetingLocationCommand.of("test-session-id", 1L, 1.3521, null)
        );

        assertEquals("Longitude cannot be null", exception.getMessage());
    }
}

