package com.geomeet.api.domain.valueobject;

import java.util.Objects;

/**
 * Session status value object.
 * Represents the current status of a session.
 */
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

    public String getValue() {
        return value;
    }

    public static SessionStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Session status cannot be null");
        }
        switch (status) {
            case "Active":
                return ACTIVE;
            case "Inactive":
                return INACTIVE;
            case "Ended":
                return ENDED;
            default:
                throw new IllegalArgumentException("Invalid session status: " + status);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionStatus that = (SessionStatus) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

