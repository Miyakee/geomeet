package com.geomeet.api.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.geomeet.api.domain.service.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BcryptPasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BcryptPasswordEncoder();
    }

    @Test
    void shouldEncodePassword() {
        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$"));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "password123";
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2);
    }

    @Test
    void shouldMatchPasswordWithCorrectHash() {
        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(rawPassword, encoded);

        assertTrue(matches);
    }

    @Test
    void shouldNotMatchPasswordWithIncorrectHash() {
        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);
        String wrongPassword = "wrongpassword";

        boolean matches = passwordEncoder.matches(wrongPassword, encoded);

        assertFalse(matches);
    }

    @Test
    void shouldNotMatchDifferentPassword() {
        String password1 = "password123";
        String password2 = "password456";
        String encoded1 = passwordEncoder.encode(password1);

        boolean matches = passwordEncoder.matches(password2, encoded1);

        assertFalse(matches);
    }

    @Test
    void shouldHandleEmptyPassword() {
        String emptyPassword = "";
        String encoded = passwordEncoder.encode(emptyPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(emptyPassword, encoded));
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String specialPassword = "p@ssw0rd!@#$%^&*()";
        String encoded = passwordEncoder.encode(specialPassword);

        assertTrue(passwordEncoder.matches(specialPassword, encoded));
    }

    @Test
    void shouldHandleLongPassword() {
        String longPassword = "a".repeat(100);
        String encoded = passwordEncoder.encode(longPassword);

        assertTrue(passwordEncoder.matches(longPassword, encoded));
    }
}

