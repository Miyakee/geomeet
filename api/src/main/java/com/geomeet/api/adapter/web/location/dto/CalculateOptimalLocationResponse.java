package com.geomeet.api.adapter.web.location.dto;

import com.geomeet.api.application.result.CalculateOptimalLocationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for optimal location calculation response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateOptimalLocationResponse {
    private Long sessionId;
    private String sessionIdString;
    private Double optimalLatitude;
    private Double optimalLongitude;
    private Double totalTravelDistance;
    private Integer participantCount;
    private String message;

    public static CalculateOptimalLocationResponse create(CalculateOptimalLocationResult result){
       return CalculateOptimalLocationResponse.builder()
            .sessionId(result.getSessionId())
            .sessionIdString(result.getSessionIdString())
            .optimalLatitude(result.getOptimalLatitude())
            .optimalLongitude(result.getOptimalLongitude())
            .totalTravelDistance(result.getTotalTravelDistance())
            .participantCount(result.getParticipantCount())
            .message(result.getMessage())
            .build();
    }
}

