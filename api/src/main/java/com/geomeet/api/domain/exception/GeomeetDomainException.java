package com.geomeet.api.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for domain exceptions.
 * Supports specifying HTTP status code for error response.
 */
public class GeomeetDomainException extends RuntimeException {

    private final Integer httpStatus;

    public GeomeetDomainException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST.value();
    }

    public GeomeetDomainException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus != null ? httpStatus.value() : HttpStatus.BAD_REQUEST.value();
    }


    public Integer getHttpStatus() {
        return httpStatus;
    }


}

