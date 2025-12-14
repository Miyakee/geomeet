package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LatitudeTest {

    @Test
    void shouldCreateLatitudeSuccessfully() {
        // Given
        Double value = 37.7749;

        // When
        Latitude latitude = Latitude.of(value);

        // Then
        assertNotNull(latitude);
        assertEquals(value, latitude.getValue());
    }

    @Test
    void shouldCreateLatitudeAtMinimumBoundary() {
        // Given
        Double value = -90.0;

        // When
        Latitude latitude = Latitude.of(value);

        // Then
        assertNotNull(latitude);
        assertEquals(value, latitude.getValue());
    }

    @Test
    void shouldCreateLatitudeAtMaximumBoundary() {
        // Given
        Double value = 90.0;

        // When
        Latitude latitude = Latitude.of(value);

        // Then
        assertNotNull(latitude);
        assertEquals(value, latitude.getValue());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Latitude.of(null)
        );

        assertEquals("Latitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsBelowMinimum() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Latitude.of(-90.1)
        );

        assertEquals("Latitude must be between -90.0 and 90.0 degrees", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsAboveMaximum() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Latitude.of(90.1)
        );

        assertEquals("Latitude must be between -90.0 and 90.0 degrees", exception.getMessage());
    }
}

