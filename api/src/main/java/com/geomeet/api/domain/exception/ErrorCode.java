package com.geomeet.api.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of common error codes and messages used across the application.
 * Each error code includes the error message and corresponding HTTP status code.
 */
@Getter
public enum ErrorCode {
    // Session related errors
    SESSION_NOT_FOUND("Session not found", HttpStatus.BAD_REQUEST),
    SESSION_ALREADY_ENDED("Cannot perform this action on an ended session", HttpStatus.BAD_REQUEST),
    SESSION_ENDED("Cannot join a session that has ended", HttpStatus.BAD_REQUEST),
    INVALID_SESSION_CODE("Invalid Session code", HttpStatus.BAD_REQUEST),
    
    // Access control errors
    ACCESS_DENIED("Access denied: User is not a participant or initiator", HttpStatus.FORBIDDEN),
    NOT_PARTICIPANT("User is not a participant in this session", HttpStatus.FORBIDDEN),
    NOT_INITIATOR("Only the session initiator can perform this action", HttpStatus.FORBIDDEN),
    CANNOT_GENERATE_INVITE_LINK("Only the session initiator can generate invite links", HttpStatus.FORBIDDEN),
    CANNOT_END_SESSION("Only the session initiator can end the session", HttpStatus.FORBIDDEN),
    
    // Invite code errors
    INVALID_INVITE_CODE("Invalid invite code", HttpStatus.BAD_REQUEST),
    
    // Location related errors
    CANNOT_UPDATE_LOCATION_ENDED("Cannot update location for an ended session", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_MEETING_LOCATION_ENDED("Cannot update meeting location for an ended session", HttpStatus.BAD_REQUEST),
    CANNOT_CALCULATE_OPTIMAL_LOCATION_ENDED("Cannot calculate optimal location for an ended session", HttpStatus.BAD_REQUEST),
    
    // User related errors
    USER_NOT_FOUND("User not found", HttpStatus.BAD_REQUEST),
    INITIATOR_NOT_FOUND("Initiator not found", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND_FOR_PARTICIPANT("User not found for participant", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_OR_USERNAME("Invalid email: existing email or username", HttpStatus.BAD_REQUEST),
    
    // Optimal location errors
    INSUFFICIENT_PARTICIPANTS("Cannot calculate optimal location. At least one participant must share their location", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }


    public GeomeetDomainException toException() {
        return new GeomeetDomainException(this.message, this.httpStatus);
    }

    public GeomeetDomainException toException(String customMessage) {
        return new GeomeetDomainException(customMessage, this.httpStatus);
    }
}

