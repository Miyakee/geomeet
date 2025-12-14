package com.geomeet.api.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Location value object.
 * Represents a geographic location with latitude and longitude.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Location {

    private final Latitude latitude;
    private final Longitude longitude;
    private final Double accuracy; // Optional: accuracy in meters

    private Location(Latitude latitude, Longitude longitude, Double accuracy) {
        if (latitude == null) {
            throw new IllegalArgumentException("Latitude cannot be null");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("Longitude cannot be null");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    /**
     * Factory method to create a Location with accuracy.
     *
     * @param latitude  the latitude value
     * @param longitude the longitude value
     * @param accuracy  the accuracy in meters (optional)
     * @return a new Location instance
     */
    public static Location of(Double latitude, Double longitude, Double accuracy) {
        return new Location(
            Latitude.of(latitude),
            Longitude.of(longitude),
            accuracy
        );
    }

    /**
     * Factory method to create a Location without accuracy.
     *
     * @param latitude  the latitude value
     * @param longitude the longitude value
     * @return a new Location instance
     */
    public static Location of(Double latitude, Double longitude) {
        return new Location(
            Latitude.of(latitude),
            Longitude.of(longitude),
            null
        );
    }
}

