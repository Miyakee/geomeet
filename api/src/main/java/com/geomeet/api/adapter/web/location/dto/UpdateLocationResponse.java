package com.geomeet.api.adapter.web.location.dto;

import com.geomeet.api.application.result.UpdateLocationResult;
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

    public static UpdateLocationResponse create(UpdateLocationResult result){
        return UpdateLocationResponse.builder()
            .participantId(result.getParticipantId())
            .sessionId(result.getSessionId())
            .sessionIdString(result.getSessionIdString())
            .userId(result.getUserId())
            .latitude(result.getLatitude())
            .longitude(result.getLongitude())
            .accuracy(result.getAccuracy())
            .updatedAt(result.getUpdatedAt())
            .message(result.getMessage())
            .build();

    }
}

