package com.geomeet.api.infrastructure.security;

import com.geomeet.api.domain.service.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Infrastructure implementation of domain PasswordEncoder port.
 * Uses BCrypt for password hashing.
 */
@Component
public class BcryptPasswordEncoder implements PasswordEncoder {

    private static final int BCRYPT_ROUNDS = 12;

    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}

