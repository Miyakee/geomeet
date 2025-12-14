package com.geomeet.api.adapter.web.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for meeting location update response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMeetingLocationResponse {
    private Long sessionId;
    private String sessionIdString;
    private Double latitude;
    private Double longitude;
    private String message;
}

