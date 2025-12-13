package com.geomeet.api.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Password hash value object.
 * Represents an encrypted password hash.
 */
@Getter
@EqualsAndHashCode
@ToString
public class PasswordHash {

    private final String value;

    public PasswordHash(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return "PasswordHash{***}";
    }
}

