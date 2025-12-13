package com.geomeet.api.domain.valueobject;

import java.util.Objects;

/**
 * Password hash value object.
 * Represents an encrypted password hash.
 */
public class PasswordHash {

    private final String value;

    public PasswordHash(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
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
        PasswordHash that = (PasswordHash) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "PasswordHash{***}";
    }
}

