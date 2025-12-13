package com.geomeet.api.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Username value object.
 * Encapsulates username validation logic.
 */
@Getter
@EqualsAndHashCode
@ToString
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
}

