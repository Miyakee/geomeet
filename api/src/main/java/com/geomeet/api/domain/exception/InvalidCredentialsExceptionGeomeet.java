package com.geomeet.api.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidCredentialsExceptionGeomeet extends GeomeetDomainException {

    public InvalidCredentialsExceptionGeomeet() {
        super("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    public InvalidCredentialsExceptionGeomeet(String message) {
        super(message);
    }
}

