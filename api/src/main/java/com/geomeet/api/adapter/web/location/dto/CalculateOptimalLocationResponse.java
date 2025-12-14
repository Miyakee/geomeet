package com.geomeet.api.adapter.web.location.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO for optimal location calculation response.
 */
@Getter
@Builder
public class CalculateOptimalLocationResponse {
    private Long sessionId;
    private String sessionIdString;
    private Double optimalLatitude;
    private Double optimalLongitude;
    private Double totalTravelDistance;
    private Integer participantCount;
    private String message;
}

