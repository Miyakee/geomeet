package com.geomeet.api.adapter.web.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for participant information.
 * Contains both participant details and location information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantInfo {
    private Long participantId;
    private Long userId;
    private String username;
    private String email;
    private String joinedAt;
    // Location information (nullable - participant may not have shared location)
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String locationUpdatedAt;
}

