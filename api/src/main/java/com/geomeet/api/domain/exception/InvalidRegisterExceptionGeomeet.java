package com.geomeet.api.domain.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidRegisterExceptionGeomeet extends GeomeetDomainException {

    public InvalidRegisterExceptionGeomeet() {
        super("Invalid register info");
    }

    public InvalidRegisterExceptionGeomeet(String message) {
        super(message);
    }
}

