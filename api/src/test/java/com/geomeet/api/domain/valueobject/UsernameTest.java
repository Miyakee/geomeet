package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UsernameTest {

    @Test
    void shouldCreateValidUsername() {
        Username username = new Username("testuser");
        assertNotNull(username);
        assertEquals("testuser", username.getValue());
    }

    @Test
    void shouldCreateUsernameWithMinimumLength() {
        Username username = new Username("abc");
        assertEquals("abc", username.getValue());
    }

    @Test
    void shouldCreateUsernameWithMaximumLength() {
        String longUsername = "a".repeat(50);
        Username username = new Username(longUsername);
        assertEquals(longUsername, username.getValue());
    }

    @Test
    void shouldCreateUsernameWithUnderscore() {
        Username username = new Username("test_user");
        assertEquals("test_user", username.getValue());
    }

    @Test
    void shouldCreateUsernameWithNumbers() {
        Username username = new Username("user123");
        assertEquals("user123", username.getValue());
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Username(null));
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Username(""));
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Username("   "));
    }

    @Test
    void shouldThrowExceptionWhenUsernameTooShort() {
        assertThrows(IllegalArgumentException.class, () -> new Username("ab"));
    }

    @Test
    void shouldThrowExceptionWhenUsernameTooLong() {
        String tooLong = "a".repeat(51);
        assertThrows(IllegalArgumentException.class, () -> new Username(tooLong));
    }

    @Test
    void shouldThrowExceptionWhenUsernameContainsInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Username("test-user"));
    }

    @Test
    void shouldThrowExceptionWhenUsernameContainsSpaces() {
        assertThrows(IllegalArgumentException.class, () -> new Username("test user"));
    }

    @Test
    void shouldThrowExceptionWhenUsernameContainsSpecialCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Username("test@user"));
    }

    @Test
    void shouldBeEqualWhenUsernamesAreSame() {
        Username username1 = new Username("testuser");
        Username username2 = new Username("testuser");
        assertEquals(username1, username2);
        assertEquals(username1.hashCode(), username2.hashCode());
    }
}

