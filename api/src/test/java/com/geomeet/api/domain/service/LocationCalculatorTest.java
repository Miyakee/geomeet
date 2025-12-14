package com.geomeet.api.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.geomeet.api.domain.valueobject.Location;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LocationCalculator domain service.
 */
class LocationCalculatorTest {

    // Singapore coordinates for testing
    private static final double SINGAPORE_LAT = 1.3521;
    private static final double SINGAPORE_LON = 103.8198;

    @Test
    void shouldCalculateGeometricCenterForSingleLocation() {
        // Given
        Location location = Location.of(SINGAPORE_LAT, SINGAPORE_LON);
        List<Location> locations = Collections.singletonList(location);

        // When
        Location result = LocationCalculator.calculateGeometricCenter(locations);

        // Then
        assertEquals(SINGAPORE_LAT, result.getLatitude().getValue(), 0.0001);
        assertEquals(SINGAPORE_LON, result.getLongitude().getValue(), 0.0001);
    }

    @Test
    void shouldCalculateGeometricCenterForTwoLocations() {
        // Given - Two locations in Singapore
        Location location1 = Location.of(1.2903, 103.8520); // Marina Bay
        Location location2 = Location.of(1.2966, 103.7764); // Jurong East
        List<Location> locations = Arrays.asList(location1, location2);

        // When
        Location result = LocationCalculator.calculateGeometricCenter(locations);

        // Then
        double expectedLat = (1.2903 + 1.2966) / 2.0;
        double expectedLon = (103.8520 + 103.7764) / 2.0;
        assertEquals(expectedLat, result.getLatitude().getValue(), 0.0001);
        assertEquals(expectedLon, result.getLongitude().getValue(), 0.0001);
    }

    @Test
    void shouldCalculateGeometricCenterForMultipleLocations() {
        // Given - Three locations in Singapore
        Location location1 = Location.of(1.2903, 103.8520); // Marina Bay
        Location location2 = Location.of(1.2966, 103.7764); // Jurong East
        Location location3 = Location.of(1.3521, 103.8198); // Central Singapore
        List<Location> locations = Arrays.asList(location1, location2, location3);

        // When
        Location result = LocationCalculator.calculateGeometricCenter(locations);

        // Then
        double expectedLat = (1.2903 + 1.2966 + 1.3521) / 3.0;
        double expectedLon = (103.8520 + 103.7764 + 103.8198) / 3.0;
        assertEquals(expectedLat, result.getLatitude().getValue(), 0.0001);
        assertEquals(expectedLon, result.getLongitude().getValue(), 0.0001);
    }

    @Test
    void shouldThrowExceptionWhenLocationsListIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            LocationCalculator.calculateGeometricCenter(null);
        });
    }

    @Test
    void shouldThrowExceptionWhenLocationsListIsEmpty() {
        // Given
        List<Location> emptyList = new ArrayList<>();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            LocationCalculator.calculateGeometricCenter(emptyList);
        });
    }

    @Test
    void shouldCalculateHaversineDistanceForSameLocation() {
        // Given
        Location location = Location.of(SINGAPORE_LAT, SINGAPORE_LON);

        // When
        double distance = LocationCalculator.calculateHaversineDistance(location, location);

        // Then
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void shouldCalculateHaversineDistanceForTwoLocations() {
        // Given - Two locations in Singapore (Marina Bay to Jurong East)
        Location location1 = Location.of(1.2903, 103.8520); // Marina Bay
        Location location2 = Location.of(1.2966, 103.7764); // Jurong East

        // When
        double distance = LocationCalculator.calculateHaversineDistance(location1, location2);

        // Then - Distance should be approximately 8-9 km
        assertTrue(distance > 7.0 && distance < 10.0, 
            "Distance should be between 7-10 km, but was: " + distance);
    }

    @Test
    void shouldCalculateHaversineDistanceForDistantLocations() {
        // Given - Singapore to Kuala Lumpur (approximately 300+ km)
        Location singapore = Location.of(1.3521, 103.8198);
        Location kualaLumpur = Location.of(3.1390, 101.6869);

        // When
        double distance = LocationCalculator.calculateHaversineDistance(singapore, kualaLumpur);

        // Then - Distance should be approximately 300-350 km
        assertTrue(distance > 300.0 && distance < 350.0,
            "Distance should be between 300-350 km, but was: " + distance);
    }

    @Test
    void shouldCalculateTotalTravelDistanceForSingleParticipant() {
        // Given
        Location participantLocation = Location.of(1.2903, 103.8520);
        Location meetingLocation = Location.of(1.2966, 103.7764);
        List<Location> participantLocations = Collections.singletonList(participantLocation);

        // When
        double totalDistance = LocationCalculator.calculateTotalTravelDistance(
            participantLocations, meetingLocation);

        // Then
        double expectedDistance = LocationCalculator.calculateHaversineDistance(
            participantLocation, meetingLocation);
        assertEquals(expectedDistance, totalDistance, 0.001);
    }

    @Test
    void shouldCalculateTotalTravelDistanceForMultipleParticipants() {
        // Given - Three participants
        Location participant1 = Location.of(1.2903, 103.8520); // Marina Bay
        Location participant2 = Location.of(1.2966, 103.7764); // Jurong East
        Location participant3 = Location.of(1.3521, 103.8198); // Central
        Location meetingLocation = Location.of(1.3000, 103.8000); // Meeting point
        List<Location> participantLocations = Arrays.asList(participant1, participant2, participant3);

        // When
        double totalDistance = LocationCalculator.calculateTotalTravelDistance(
            participantLocations, meetingLocation);

        // Then
        double expectedDistance = 
            LocationCalculator.calculateHaversineDistance(participant1, meetingLocation) +
            LocationCalculator.calculateHaversineDistance(participant2, meetingLocation) +
            LocationCalculator.calculateHaversineDistance(participant3, meetingLocation);
        assertEquals(expectedDistance, totalDistance, 0.01);
    }

    @Test
    void shouldReturnZeroForEmptyParticipantLocations() {
        // Given
        Location meetingLocation = Location.of(SINGAPORE_LAT, SINGAPORE_LON);
        List<Location> emptyList = new ArrayList<>();

        // When
        double totalDistance = LocationCalculator.calculateTotalTravelDistance(
            emptyList, meetingLocation);

        // Then
        assertEquals(0.0, totalDistance, 0.001);
    }

    @Test
    void shouldReturnZeroForNullParticipantLocations() {
        // Given
        Location meetingLocation = Location.of(SINGAPORE_LAT, SINGAPORE_LON);

        // When
        double totalDistance = LocationCalculator.calculateTotalTravelDistance(
            null, meetingLocation);

        // Then
        assertEquals(0.0, totalDistance, 0.001);
    }

    @Test
    void shouldCalculateOptimalLocationMinimizingTotalDistance() {
        // Given - Three participants forming a triangle
        Location participant1 = Location.of(1.2903, 103.8520); // Marina Bay
        Location participant2 = Location.of(1.2966, 103.7764); // Jurong East
        Location participant3 = Location.of(1.3521, 103.8198); // Central
        List<Location> participantLocations = Arrays.asList(participant1, participant2, participant3);

        // When - Calculate geometric center
        Location optimalLocation = LocationCalculator.calculateGeometricCenter(participantLocations);
        double totalDistance = LocationCalculator.calculateTotalTravelDistance(
            participantLocations, optimalLocation);

        // Then - Total distance should be reasonable (not too large)
        // For Singapore, total distance for 3 participants should be less than 30 km
        assertTrue(totalDistance < 30.0,
            "Total distance should be reasonable for Singapore, but was: " + totalDistance);
    }
}

