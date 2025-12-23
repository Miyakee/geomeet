package com.geomeet.api.domain.exception;

/**
 * Base class for domain exceptions.
 */
public class GeomeetDomainException extends RuntimeException {

    public GeomeetDomainException(String message) {
        super(message);
    }

    public GeomeetDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

