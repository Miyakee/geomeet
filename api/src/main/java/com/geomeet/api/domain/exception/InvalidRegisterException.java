package com.geomeet.api.domain.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidRegisterException extends DomainException {

    public InvalidRegisterException() {
        super("Invalid register info");
    }

    public InvalidRegisterException(String message) {
        super(message);
    }
}

