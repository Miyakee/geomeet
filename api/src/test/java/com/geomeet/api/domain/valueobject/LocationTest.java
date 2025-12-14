package com.geomeet.api.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
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

    @Test
    void shouldThrowExceptionWhenLongitudeIsNullWithoutAccuracy() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Location.of(37.7749, null)
        );

        assertEquals("Longitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsNullWithoutAccuracy() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Location.of(null, -122.4194)
        );

        assertEquals("Latitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldCreateLocationWithNullAccuracy() {
        // Given
        Double latitude = 37.7749;
        Double longitude = -122.4194;
        Double accuracy = null;

        // When
        Location location = Location.of(latitude, longitude, accuracy);

        // Then
        assertNotNull(location);
        assertEquals(latitude, location.getLatitude().getValue());
        assertEquals(longitude, location.getLongitude().getValue());
        assertEquals(null, location.getAccuracy());
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        // Given
        Location location1 = Location.of(37.7749, -122.4194, 10.0);
        Location location2 = Location.of(37.7749, -122.4194, 10.0);
        Location location3 = Location.of(37.7749, -122.4194, null);

        // Then
        assertEquals(location1, location2);
        assertEquals(location1.hashCode(), location2.hashCode());
        assertFalse(location1.equals(location3));
    }

    @Test
    void shouldTestToString() {
        // Given
        Location location = Location.of(37.7749, -122.4194, 10.0);

        // When
        String toString = location.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("37.7749") || toString.contains("Location"));
    }

    @Test
    void shouldThrowExceptionWhenBothLatitudeAndLongitudeAreNull() {
        // When & Then - latitude null check happens first
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Location.of(null, null, 10.0)
        );

        assertEquals("Latitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBothLatitudeAndLongitudeAreNullWithoutAccuracy() {
        // When & Then - latitude null check happens first
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Location.of(null, null)
        );

        assertEquals("Latitude cannot be null", exception.getMessage());
    }

    @Test
    void shouldTestNotEquals() {
        // Given
        Location location1 = Location.of(37.7749, -122.4194, 10.0);
        Location location2 = Location.of(37.7750, -122.4194, 10.0); // Different latitude
        Location location3 = Location.of(37.7749, -122.4195, 10.0); // Different longitude
        Location location4 = Location.of(37.7749, -122.4194, 20.0); // Different accuracy

        // Then
        assertFalse(location1.equals(location2));
        assertFalse(location1.equals(location3));
        assertFalse(location1.equals(location4));
        assertFalse(location1.equals(null));
        assertFalse(location1.equals("not a location"));
    }

    @Test
    void shouldTestHashCodeConsistency() {
        // Given
        Location location1 = Location.of(37.7749, -122.4194, 10.0);
        Location location2 = Location.of(37.7749, -122.4194, 10.0);

        // Then
        assertEquals(location1.hashCode(), location2.hashCode());
        // Hash code should be consistent
        assertEquals(location1.hashCode(), location1.hashCode());
    }

    @Test
    void shouldThrowExceptionWhenLatitudeIsNullInConstructor() throws Exception {
        // Given - Use reflection to test private constructor directly
        Constructor<Location> constructor = Location.class.getDeclaredConstructor(
            Latitude.class, Longitude.class, Double.class
        );
        constructor.setAccessible(true);

        Longitude longitude = Longitude.of(-122.4194);
        Double accuracy = 10.0;

        // When & Then
        Exception exception = assertThrows(
            Exception.class,
            () -> constructor.newInstance(null, longitude, accuracy)
        );

        // Reflection wraps the exception in InvocationTargetException
        Throwable cause = exception.getCause();
        if (cause == null) {
            cause = exception;
        }
        assertTrue(cause instanceof IllegalArgumentException);
        assertEquals("Latitude cannot be null", cause.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLongitudeIsNullInConstructor() throws Exception {
        // Given - Use reflection to test private constructor directly
        Constructor<Location> constructor = Location.class.getDeclaredConstructor(
            Latitude.class, Longitude.class, Double.class
        );
        constructor.setAccessible(true);

        Latitude latitude = Latitude.of(37.7749);
        Double accuracy = 10.0;

        // When & Then
        Exception exception = assertThrows(
            Exception.class,
            () -> constructor.newInstance(latitude, null, accuracy)
        );

        // Reflection wraps the exception in InvocationTargetException
        Throwable cause = exception.getCause();
        if (cause == null) {
            cause = exception;
        }
        assertTrue(cause instanceof IllegalArgumentException);
        assertEquals("Longitude cannot be null", cause.getMessage());
    }

    @Test
    void shouldCreateLocationThroughConstructorWithReflection() throws Exception {
        // Given - Use reflection to test private constructor directly
        Constructor<Location> constructor = Location.class.getDeclaredConstructor(
            Latitude.class, Longitude.class, Double.class
        );
        constructor.setAccessible(true);

        Latitude latitude = Latitude.of(37.7749);
        Longitude longitude = Longitude.of(-122.4194);
        Double accuracy = 10.0;

        // When
        Location location = constructor.newInstance(latitude, longitude, accuracy);

        // Then
        assertNotNull(location);
        assertEquals(37.7749, location.getLatitude().getValue());
        assertEquals(-122.4194, location.getLongitude().getValue());
        assertEquals(accuracy, location.getAccuracy());
    }

    @Test
    void shouldCreateLocationThroughConstructorWithReflectionWithoutAccuracy() throws Exception {
        // Given - Use reflection to test private constructor directly
        Constructor<Location> constructor = Location.class.getDeclaredConstructor(
            Latitude.class, Longitude.class, Double.class
        );
        constructor.setAccessible(true);

        Latitude latitude = Latitude.of(37.7749);
        Longitude longitude = Longitude.of(-122.4194);

        // When
        Location location = constructor.newInstance(latitude, longitude, null);

        // Then
        assertNotNull(location);
        assertEquals(37.7749, location.getLatitude().getValue());
        assertEquals(-122.4194, location.getLongitude().getValue());
        assertEquals(null, location.getAccuracy());
    }
}

