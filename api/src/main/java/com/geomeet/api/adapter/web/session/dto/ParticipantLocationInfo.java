package com.geomeet.api.adapter.web.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for participant location information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantLocationInfo {
    private Long participantId;
    private Long userId;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String updatedAt;
}

