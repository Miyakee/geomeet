package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for update location use case.
 * Represents the input for updating a participant's location.
 */
@Getter
@Builder
public class UpdateLocationCommand {

    private final String sessionId; // SessionId value (UUID string)
    private final Long userId; // User ID who is updating location
    private final Double latitude; // Latitude coordinate
    private final Double longitude; // Longitude coordinate
    private final Double accuracy; // Optional: accuracy in meters

    public UpdateLocationCommand(String sessionId, Long userId, Double latitude, Double longitude, Double accuracy) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (latitude == null) {
            throw new IllegalArgumentException("Latitude cannot be null");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("Longitude cannot be null");
        }
        this.sessionId = sessionId;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    /**
     * Factory method to create an UpdateLocationCommand.
     *
     * @param sessionId the session ID string
     * @param userId the user ID updating the location
     * @param latitude the latitude value
     * @param longitude the longitude value
     * @param accuracy the accuracy in meters (optional)
     * @return a new UpdateLocationCommand
     */
    public static UpdateLocationCommand of(
            String sessionId, Long userId, Double latitude, Double longitude, Double accuracy) {
        return UpdateLocationCommand.builder()
            .sessionId(sessionId)
            .userId(userId)
            .latitude(latitude)
            .longitude(longitude)
            .accuracy(accuracy)
            .build();
    }

    /**
     * Factory method to create an UpdateLocationCommand without accuracy.
     *
     * @param sessionId the session ID string
     * @param userId the user ID updating the location
     * @param latitude the latitude value
     * @param longitude the longitude value
     * @return a new UpdateLocationCommand
     */
    public static UpdateLocationCommand of(String sessionId, Long userId, Double latitude, Double longitude) {
        return UpdateLocationCommand.builder()
            .sessionId(sessionId)
            .userId(userId)
            .latitude(latitude)
            .longitude(longitude)
            .accuracy(null)
            .build();
    }
}

