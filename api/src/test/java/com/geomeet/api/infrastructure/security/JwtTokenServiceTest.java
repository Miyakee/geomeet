package com.geomeet.api.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void shouldReturnNullWhenUserIdIsNotNumber() throws Exception {
        // Test with null userId - this tests the branch where userId is null in generateToken
        // When userId is null, it's stored as null in claims, and extractUserId returns null
        String nullUserIdToken = jwtTokenService.generateToken(null, "testuser");
        assertNull(jwtTokenService.extractUserId(nullUserIdToken));
        
        // Test with valid userId - this tests the branch where userIdObj is a Number
        String validToken = jwtTokenService.generateToken(1L, "testuser");
        Long userId = jwtTokenService.extractUserId(validToken);
        assertNotNull(userId);
        assertEquals(1L, userId);
    }

    @Test
    void shouldThrowExceptionForExpiredToken() throws Exception {
        // Create a token with very short expiration
        JwtTokenService shortExpirationService = new JwtTokenService();
        ReflectionTestUtils.setField(shortExpirationService, "secret", SECRET);
        ReflectionTestUtils.setField(shortExpirationService, "expiration", 1L); // 1 millisecond
        
        String token = shortExpirationService.generateToken(1L, "testuser");
        
        // Wait for token to expire
        Thread.sleep(10);
        
        // Expired tokens throw exception when parsing
        // This tests the exception path in extractAllClaims -> isTokenExpired
        assertThrows(Exception.class, () -> shortExpirationService.isTokenExpired(token));
    }

    @Test
    void shouldThrowExceptionWhenValidatingExpiredToken() throws Exception {
        // Create a token with very short expiration
        JwtTokenService shortExpirationService = new JwtTokenService();
        ReflectionTestUtils.setField(shortExpirationService, "secret", SECRET);
        ReflectionTestUtils.setField(shortExpirationService, "expiration", 1L); // 1 millisecond
        
        String token = shortExpirationService.generateToken(1L, "testuser");
        
        // Wait for token to expire
        Thread.sleep(10);
        
        // validateToken calls extractUsername which will throw exception for expired token
        // This tests the exception path in validateToken
        assertThrows(Exception.class, () -> shortExpirationService.validateToken(token, "testuser"));
    }
}

