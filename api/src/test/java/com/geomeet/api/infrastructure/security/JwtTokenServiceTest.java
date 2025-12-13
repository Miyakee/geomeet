package com.geomeet.api.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private static final String SECRET = "geomeet-secret-key-for-testing-purposes-only-must-be-long-enough";
    private static final Long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtTokenService, "expiration", EXPIRATION);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtTokenService.generateToken(1L, "testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtTokenService.generateToken(1L, "testuser");

        String username = jwtTokenService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtTokenService.generateToken(123L, "testuser");

        Long userId = jwtTokenService.extractUserId(token);

        assertEquals(123L, userId);
    }

    @Test
    void shouldExtractExpirationFromToken() {
        String token = jwtTokenService.generateToken(1L, "testuser");

        Date expiration = jwtTokenService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void shouldValidateTokenWithCorrectUsername() {
        String token = jwtTokenService.generateToken(1L, "testuser");

        boolean isValid = jwtTokenService.validateToken(token, "testuser");

        assertTrue(isValid);
    }

    @Test
    void shouldNotValidateTokenWithIncorrectUsername() {
        String token = jwtTokenService.generateToken(1L, "testuser");

        boolean isValid = jwtTokenService.validateToken(token, "wronguser");

        assertFalse(isValid);
    }

    @Test
    void shouldNotBeExpiredForValidToken() {
        String token = jwtTokenService.generateToken(1L, "testuser");

        boolean isExpired = jwtTokenService.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void shouldGenerateDifferentTokensForSameUser() {
        String token1 = jwtTokenService.generateToken(1L, "testuser");
        String token2 = jwtTokenService.generateToken(1L, "testuser");

        assertNotEquals(token1, token2);
    }

    @Test
    void shouldHandleNullUserId() {
        String token = jwtTokenService.generateToken(null, "testuser");

        Long userId = jwtTokenService.extractUserId(token);

        assertNull(userId);
    }
}

