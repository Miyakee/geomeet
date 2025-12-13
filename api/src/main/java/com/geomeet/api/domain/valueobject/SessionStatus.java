package com.geomeet.api.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Session status value object.
 * Represents the current status of a session.
 */
@Getter
@EqualsAndHashCode
@ToString
public class SessionStatus {

    public static final SessionStatus ACTIVE = new SessionStatus("Active");
    public static final SessionStatus INACTIVE = new SessionStatus("Inactive");
    public static final SessionStatus ENDED = new SessionStatus("Ended");

    private final String value;

    private SessionStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Session status cannot be null or empty");
        }
        this.value = value;
    }

    public static SessionStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Session status cannot be null");
        }
      return switch (status.toUpperCase()) {
        case "ACTIVE" -> ACTIVE;
        case "INACTIVE" -> INACTIVE;
        case "ENDED" -> ENDED;
        default -> throw new IllegalArgumentException("Invalid session status: " + status);
      };
    }
}

