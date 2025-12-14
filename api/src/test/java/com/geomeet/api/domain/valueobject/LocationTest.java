package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LocationTest {

    @Test
    void shouldCreateLocationWithAccuracy() {
        // Given
        Double latitude = 37.7749;
        Double longitude = -122.4194;
        Double accuracy = 10.0;

        // When
        Location location = Location.of(latitude, longitude, accuracy);

        // Then
        assertNotNull(location);
        assertEquals(latitude, location.getLatitude().getValue());
        assertEquals(longitude, location.getLongitude().getValue());
        assertEquals(accuracy, location.getAccuracy());
    }

    @Test
    void shouldCreateLocationWithoutAccuracy() {
        // Given
        Double latitude = 37.7749;
        Double longitude = -122.4194;

        // When
        Location location = Location.of(latitude, longitude);

        // Then
        assertNotNull(location);
        assertEquals(latitude, location.getLatitude().getValue());
        assertEquals(longitude, location.getLongitude().getValue());
        assertEquals(null, location.getAccuracy());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Location.of(null, -122.4194, 10.0)
        );

        assertEquals("Latitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Location.of(37.7749, null, 10.0)
        );

        assertEquals("Longitude cannot be null", exception.getMessage());
    }
}

