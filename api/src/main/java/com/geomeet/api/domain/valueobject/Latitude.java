package com.geomeet.api.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Latitude value object.
 * Represents a latitude coordinate with validation.
 * Valid range: -90.0 to 90.0 degrees.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Latitude {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;

    private final Double value;

    private Latitude(Double value) {
        if (value == null) {
            throw new IllegalArgumentException("Latitude cannot be null");
        }
        if (value < MIN_LATITUDE || value > MAX_LATITUDE) {
            throw new IllegalArgumentException(
                String.format("Latitude must be between %.1f and %.1f degrees", MIN_LATITUDE, MAX_LATITUDE)
            );
        }
        this.value = value;
    }

    /**
     * Factory method to create a Latitude.
     *
     * @param value the latitude value
     * @return a new Latitude instance
     * @throws IllegalArgumentException if value is invalid
     */
    public static Latitude of(Double value) {
        return new Latitude(value);
    }
}

