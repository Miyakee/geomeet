package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for update location use case.
 * Represents the output after updating a participant's location.
 */
@Getter
@Builder
public class UpdateLocationResult {

    private final Long participantId;
    private final Long sessionId;
    private final String sessionIdString;
    private final Long userId;
    private final Double latitude;
    private final Double longitude;
    private final Double accuracy;
    private final String updatedAt;
    private final String message;
}

