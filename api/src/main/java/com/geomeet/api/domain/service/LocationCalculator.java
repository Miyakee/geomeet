package com.geomeet.api.domain.service;

import com.geomeet.api.domain.valueobject.Location;
import java.util.List;

/**
 * Domain service for calculating optimal meeting locations.
 * This service encapsulates the business logic for location calculations.
 */
public class LocationCalculator {

    /**
     * Calculates the geometric center (centroid) of multiple locations.
     * This uses simple averaging of latitude and longitude, which is a good approximation
     * for small areas like Singapore (where the error is minimal).
     * 
     * Note: This minimizes Euclidean distance in coordinate space, not actual travel distance.
     * For more accuracy, we could use Haversine-based optimization, but that requires
     * iterative algorithms and is computationally expensive.
     * 
     * The calculated distances (using calculateHaversineDistance) provide accurate
     * travel distance information even though the optimal location is calculated using
     * the simpler geometric center method.
     *
     * @param locations list of participant locations
     * @return optimal location (centroid)
     * @throws IllegalArgumentException if locations list is null or empty
     */
    public static Location calculateGeometricCenter(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Locations list cannot be null or empty");
        }

        if (locations.size() == 1) {
            return locations.get(0);
        }

        double sumLatitude = 0.0;
        double sumLongitude = 0.0;

        for (Location location : locations) {
            sumLatitude += location.getLatitude().getValue();
            sumLongitude += location.getLongitude().getValue();
        }

        double avgLatitude = sumLatitude / locations.size();
        double avgLongitude = sumLongitude / locations.size();

        return Location.of(avgLatitude, avgLongitude);
    }

    /**
     * Calculates the Haversine distance between two locations in kilometers.
     * This is more accurate for larger distances but computationally more expensive.
     *
     * @param location1 first location
     * @param location2 second location
     * @return distance in kilometers
     */
    public static double calculateHaversineDistance(Location location1, Location location2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double lat1 = Math.toRadians(location1.getLatitude().getValue());
        double lon1 = Math.toRadians(location1.getLongitude().getValue());
        double lat2 = Math.toRadians(location2.getLatitude().getValue());
        double lon2 = Math.toRadians(location2.getLongitude().getValue());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2)
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates the total travel distance for all participants to a given location.
     *
     * @param participantLocations list of participant locations
     * @param meetingLocation the proposed meeting location
     * @return total distance in kilometers
     */
    public static double calculateTotalTravelDistance(List<Location> participantLocations, Location meetingLocation) {
        if (participantLocations == null || participantLocations.isEmpty()) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (Location participantLocation : participantLocations) {
            totalDistance += calculateHaversineDistance(participantLocation, meetingLocation);
        }

        return totalDistance;
    }
}

