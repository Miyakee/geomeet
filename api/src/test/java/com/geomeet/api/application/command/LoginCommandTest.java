package com.geomeet.api.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LoginCommandTest {

    @Test
    void shouldCreateLoginCommand() {
        LoginCommand command = new LoginCommand("testuser", "password123");
        assertNotNull(command);
        assertEquals("testuser", command.getUsernameOrEmail());
        assertEquals("password123", command.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenUsernameOrEmailIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand(null, "password123"));
    }

    @Test
    void shouldThrowExceptionWhenUsernameOrEmailIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand("", "password123"));
    }

    @Test
    void shouldThrowExceptionWhenUsernameOrEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand("   ", "password123"));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand("testuser", null));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand("testuser", ""));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new LoginCommand("testuser", "   "));
    }
}

