package com.geomeet.api.adapter.web.location.dto;

import com.geomeet.api.application.result.UpdateMeetingLocationResult;
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

  public static UpdateMeetingLocationResponse create(UpdateMeetingLocationResult result) {
    return UpdateMeetingLocationResponse.builder()
        .sessionId(result.getSessionId())
        .sessionIdString(result.getSessionIdString())
        .latitude(result.getLatitude())
        .longitude(result.getLongitude())
        .message(result.getMessage())
        .build();
  }
}

