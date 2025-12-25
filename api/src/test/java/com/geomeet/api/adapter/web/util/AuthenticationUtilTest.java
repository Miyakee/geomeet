package com.geomeet.api.adapter.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class AuthenticationUtilTest {

    @Test
    void shouldExtractUserIdFromAuthentication() {
        // Given
        Long userId = 123L;
        Authentication authentication = createMockAuthentication(userId);

        // When
        Long result = AuthenticationUtil.getUserId(authentication);

        // Then
        assertEquals(userId, result);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AuthenticationUtil.getUserId(null);
        });
        assertEquals("Authentication or principal is null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPrincipalIsNull() {
        // Given
        Authentication authentication = createMockAuthenticationWithNullPrincipal();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AuthenticationUtil.getUserId(authentication);
        });
        assertEquals("Authentication or principal is null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPrincipalIsNotLong() {
        // Given
        String principal = "not-a-long";
        Authentication authentication = createMockAuthenticationWithPrincipal(principal);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AuthenticationUtil.getUserId(authentication);
        });
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Principal is not a Long"));
    }

    @Test
    void shouldHandleDifferentLongValues() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 999L;
        Long userId3 = -1L;

        // When & Then
        assertEquals(userId1, AuthenticationUtil.getUserId(createMockAuthentication(userId1)));
        assertEquals(userId2, AuthenticationUtil.getUserId(createMockAuthentication(userId2)));
        assertEquals(userId3, AuthenticationUtil.getUserId(createMockAuthentication(userId3)));
    }

    // Helper methods to create mock Authentication objects
    private Authentication createMockAuthentication(Long userId) {
        return new Authentication() {
            @Override
            public Object getPrincipal() {
                return userId;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };
    }

    private Authentication createMockAuthenticationWithNullPrincipal() {
        return new Authentication() {
            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };
    }

    private Authentication createMockAuthenticationWithPrincipal(Object principal) {
        return new Authentication() {
            @Override
            public Object getPrincipal() {
                return principal;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
        };
    }
}

