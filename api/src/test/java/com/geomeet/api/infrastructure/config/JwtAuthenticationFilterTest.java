package com.geomeet.api.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.infrastructure.security.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtTokenService jwtTokenService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenService = mock(JwtTokenService.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthenticationWhenValidTokenProvided() throws Exception {
        // Given
        String token = "valid-token";
        String username = "testuser";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(jwtTokenService.extractUserId(token)).thenReturn(userId);
        when(jwtTokenService.validateToken(token, username)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userId, authentication.getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenNoAuthorizationHeader() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(jwtTokenService, never()).extractUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenInvalidTokenFormat() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(jwtTokenService, never()).extractUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenTokenValidationFails() throws Exception {
        // Given
        String token = "invalid-token";
        String username = "testuser";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(jwtTokenService.extractUserId(token)).thenReturn(userId);
        when(jwtTokenService.validateToken(token, username)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenTokenExtractionThrowsException() throws Exception {
        // Given
        String token = "invalid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenUsernameIsNull() throws Exception {
        // Given
        String token = "token-with-null-username";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(jwtTokenService, never()).validateToken(anyString(), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenAlreadyAuthenticated() throws Exception {
        // Given
        String token = "valid-token";
        String username = "testuser";
        Long userId = 1L;

        // Set existing authentication
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(jwtTokenService.extractUserId(token)).thenReturn(userId);
        when(jwtTokenService.validateToken(token, username)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // Should not override existing authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(existingAuth, authentication);
        verify(filterChain).doFilter(request, response);
    }
}

