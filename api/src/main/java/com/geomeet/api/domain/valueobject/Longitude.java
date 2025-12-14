package com.geomeet.api.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Longitude value object.
 * Represents a longitude coordinate with validation.
 * Valid range: -180.0 to 180.0 degrees.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Longitude {

    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    private final Double value;

    private Longitude(Double value) {
        if (value == null) {
            throw new IllegalArgumentException("Longitude cannot be null");
        }
        if (value < MIN_LONGITUDE || value > MAX_LONGITUDE) {
            throw new IllegalArgumentException(
                String.format("Longitude must be between %.1f and %.1f degrees", MIN_LONGITUDE, MAX_LONGITUDE)
            );
        }
        this.value = value;
    }

    /**
     * Factory method to create a Longitude.
     *
     * @param value the longitude value
     * @return a new Longitude instance
     * @throws IllegalArgumentException if value is invalid
     */
    public static Longitude of(Double value) {
        return new Longitude(value);
    }
}

