package com.geomeet.api.adapter.web.location.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for meeting location update request.
 * Only the session initiator can update the meeting location.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeetingLocationRequest {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}

