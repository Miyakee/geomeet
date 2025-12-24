package com.geomeet.api.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JoinSessionCommandTest {

    @Test
    void shouldCreateJoinSessionCommandSuccessfully() {
        // Given
        String sessionId = "test-session-id";
        String inviteCode = "ABC123";
        Long userId = 1L;

        // When
        JoinSessionCommand command = JoinSessionCommand.of(sessionId, inviteCode, userId);

        // Then
        assertNotNull(command);
        assertEquals(sessionId, command.getSessionId());
        assertEquals(inviteCode, command.getInviteCode());
        assertEquals(userId, command.getUserId());
    }

    @Test
    void shouldCreateJoinSessionCommandUsingBuilder() {
        // Given
        String sessionId = "test-session-id";
        String inviteCode = "ABC123";
        Long userId = 1L;

        // When
        JoinSessionCommand command = JoinSessionCommand.builder()
            .sessionId(sessionId)
            .inviteCode(inviteCode)
            .userId(userId)
            .build();

        // Then
        assertNotNull(command);
        assertEquals(sessionId, command.getSessionId());
        assertEquals(inviteCode, command.getInviteCode());
        assertEquals(userId, command.getUserId());
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JoinSessionCommand(null, "ABC123", 1L)
        );

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JoinSessionCommand("   ", "ABC123", 1L)
        );

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JoinSessionCommand("test-session-id", "ABC123", null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInviteCodeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JoinSessionCommand("test-session-id", null, 1L)
        );

        assertEquals("Invite code cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInviteCodeIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JoinSessionCommand("test-session-id", "   ", 1L)
        );

        assertEquals("Invite code cannot be null or empty", exception.getMessage());
    }
}

