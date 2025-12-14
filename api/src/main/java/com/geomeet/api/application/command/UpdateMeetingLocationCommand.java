package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for updating meeting location.
 * Represents the input for updating meeting location use case.
 */
@Getter
@Builder
public class UpdateMeetingLocationCommand {

    private final String sessionId;
    private final Long userId;
    private final Double latitude;
    private final Double longitude;

    private UpdateMeetingLocationCommand(String sessionId, Long userId, Double latitude, Double longitude) {
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
    }

    public static UpdateMeetingLocationCommand of(String sessionId, Long userId, Double latitude, Double longitude) {
        return new UpdateMeetingLocationCommand(sessionId, userId, latitude, longitude);
    }
}

