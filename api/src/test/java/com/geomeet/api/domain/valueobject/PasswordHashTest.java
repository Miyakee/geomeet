package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PasswordHashTest {

    @Test
    void shouldCreateValidPasswordHash() {
        PasswordHash passwordHash = new PasswordHash("$2a$12$hashedpassword");
        assertNotNull(passwordHash);
        assertEquals("$2a$12$hashedpassword", passwordHash.getValue());
    }

    @Test
    void shouldThrowExceptionWhenPasswordHashIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new PasswordHash(null));
    }

    @Test
    void shouldThrowExceptionWhenPasswordHashIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new PasswordHash(""));
    }

    @Test
    void shouldThrowExceptionWhenPasswordHashIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new PasswordHash("   "));
    }

    @Test
    void shouldBeEqualWhenPasswordHashesAreSame() {
        PasswordHash hash1 = new PasswordHash("$2a$12$hashedpassword");
        PasswordHash hash2 = new PasswordHash("$2a$12$hashedpassword");
        assertEquals(hash1, hash2);
        assertEquals(hash1.hashCode(), hash2.hashCode());
    }

    @Test
    void shouldNotRevealPasswordInToString() {
        PasswordHash passwordHash = new PasswordHash("$2a$12$hashedpassword");
        String toString = passwordHash.toString();
        assertEquals("PasswordHash{***}", toString);
    }
}

