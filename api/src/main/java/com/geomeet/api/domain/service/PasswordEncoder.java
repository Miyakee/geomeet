package com.geomeet.api.domain.service;

/**
 * Domain service interface for password encoding.
 * This is a port defined in the domain layer.
 */
public interface PasswordEncoder {

    /**
     * Encodes a raw password.
     *
     * @param rawPassword the raw password
     * @return the encoded password
     */
    String encode(String rawPassword);

    /**
     * Verifies a raw password against an encoded password.
     *
     * @param rawPassword the raw password to verify
     * @param encodedPassword the encoded password from storage
     * @return true if the raw password matches the encoded password
     */
    boolean matches(String rawPassword, String encodedPassword);
}

