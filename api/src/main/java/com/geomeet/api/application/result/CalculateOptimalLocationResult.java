package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for calculate optimal location use case.
 * Represents the output after calculating optimal meeting location.
 */
@Getter
@Builder
public class CalculateOptimalLocationResult {

    private final Long sessionId;
    private final String sessionIdString;
    private final Double optimalLatitude;
    private final Double optimalLongitude;
    private final Double totalTravelDistance; // Total distance in kilometers
    private final Integer participantCount;
    private final String message;
}

