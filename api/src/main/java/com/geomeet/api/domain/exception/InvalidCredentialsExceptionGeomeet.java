package com.geomeet.api.domain.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidCredentialsExceptionGeomeet extends GeomeetDomainException {

    public InvalidCredentialsExceptionGeomeet() {
        super("Invalid credentials");
    }

    public InvalidCredentialsExceptionGeomeet(String message) {
        super(message);
    }
}

