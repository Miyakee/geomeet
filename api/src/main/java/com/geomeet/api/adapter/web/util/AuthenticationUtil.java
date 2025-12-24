package com.geomeet.api.adapter.web.util;

import org.springframework.security.core.Authentication;

/**
 * Utility class for extracting information from Spring Security Authentication.
 * Reduces boilerplate code in controllers.
 */
public final class AuthenticationUtil {

    private AuthenticationUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Extract user ID from Authentication principal.
     * 
     * @param authentication the Spring Security Authentication object
     * @return the user ID as Long
     * @throws IllegalArgumentException if authentication is null or principal is not a Long
     */
    public static Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication or principal is null");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        throw new IllegalArgumentException("Principal is not a Long: " + principal.getClass());
    }
}

