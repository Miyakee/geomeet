package com.geomeet.api.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to authenticate an inactive user.
 */
public class InactiveUserExceptionGeomeet extends GeomeetDomainException {

    public InactiveUserExceptionGeomeet() {
        super("User account is inactive", HttpStatus.UNAUTHORIZED);
    }

    public InactiveUserExceptionGeomeet(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

