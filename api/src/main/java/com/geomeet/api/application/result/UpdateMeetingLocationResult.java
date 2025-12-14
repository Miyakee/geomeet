package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for update meeting location use case.
 * Represents the output after updating meeting location.
 */
@Getter
@Builder
public class UpdateMeetingLocationResult {

    private final Long sessionId;
    private final String sessionIdString;
    private final Double latitude;
    private final Double longitude;
    private final String message;
}

