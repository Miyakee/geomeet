package com.geomeet.api.domain.valueobject;

import java.util.Objects;

/**
 * Username value object.
 * Encapsulates username validation logic.
 */
public class Username {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;
    private static final String VALID_PATTERN = "^[a-zA-Z0-9_]+$";

    private final String value;

    public Username(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Username must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH)
            );
        }
        if (!value.matches(VALID_PATTERN)) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
        }
        this.value = value;
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
        Username username = (Username) o;
        return Objects.equals(value, username.value);
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

