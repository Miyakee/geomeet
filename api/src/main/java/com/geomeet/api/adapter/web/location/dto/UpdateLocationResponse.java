package com.geomeet.api.adapter.web.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location update response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLocationResponse {
    private Long participantId;
    private Long sessionId;
    private String sessionIdString;
    private Long userId;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String updatedAt;
    private String message;
}

