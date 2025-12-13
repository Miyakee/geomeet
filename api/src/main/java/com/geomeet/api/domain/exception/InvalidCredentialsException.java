package com.geomeet.api.domain.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

