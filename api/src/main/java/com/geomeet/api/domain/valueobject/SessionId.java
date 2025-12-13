package com.geomeet.api.domain.valueobject;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Session ID value object.
 * Represents a unique identifier for a session.
 */
@Getter
@EqualsAndHashCode
@ToString
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


    /**
     * Factory method to create SessionId from a string value.
     * @param value the string value of the session ID
     * @return a SessionId instance
     */
    public static SessionId fromString(String value) {
        return new SessionId(value);
    }
}

