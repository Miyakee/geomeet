package com.geomeet.api.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Session ID value object.
 * Represents a unique identifier for a session.
 */
public class SessionId {

    private final String value;

    public SessionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        this.value = value;
    }

    /**
     * Factory method to generate a new unique session ID.
     */
    public static SessionId generate() {
        return new SessionId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
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

