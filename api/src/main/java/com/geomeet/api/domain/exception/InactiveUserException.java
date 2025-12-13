package com.geomeet.api.domain.exception;

/**
 * Exception thrown when attempting to authenticate an inactive user.
 */
public class InactiveUserException extends DomainException {

    public InactiveUserException() {
        super("User account is inactive");
    }

    public InactiveUserException(String message) {
        super(message);
    }
}

