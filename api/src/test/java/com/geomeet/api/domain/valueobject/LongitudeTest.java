package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LongitudeTest {

    @Test
    void shouldCreateLongitudeSuccessfully() {
        // Given
        Double value = -122.4194;

        // When
        Longitude longitude = Longitude.of(value);

        // Then
        assertNotNull(longitude);
        assertEquals(value, longitude.getValue());
    }

    @Test
    void shouldCreateLongitudeAtMinimumBoundary() {
        // Given
        Double value = -180.0;

        // When
        Longitude longitude = Longitude.of(value);

        // Then
        assertNotNull(longitude);
        assertEquals(value, longitude.getValue());
    }

    @Test
    void shouldCreateLongitudeAtMaximumBoundary() {
        // Given
        Double value = 180.0;

        // When
        Longitude longitude = Longitude.of(value);

        // Then
        assertNotNull(longitude);
        assertEquals(value, longitude.getValue());
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Longitude.of(null)
        );

        assertEquals("Longitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsBelowMinimum() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Longitude.of(-180.1)
        );

        assertEquals("Longitude must be between -180.0 and 180.0 degrees", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsAboveMaximum() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Longitude.of(180.1)
        );

        assertEquals("Longitude must be between -180.0 and 180.0 degrees", exception.getMessage());
    }
}

