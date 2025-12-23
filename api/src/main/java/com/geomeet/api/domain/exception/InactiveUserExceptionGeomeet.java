package com.geomeet.api.domain.exception;

/**
 * Exception thrown when attempting to authenticate an inactive user.
 */
public class InactiveUserExceptionGeomeet extends GeomeetDomainException {

    public InactiveUserExceptionGeomeet() {
        super("User account is inactive");
    }

    public InactiveUserExceptionGeomeet(String message) {
        super(message);
    }
}

