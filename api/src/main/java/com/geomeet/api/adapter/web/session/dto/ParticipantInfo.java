package com.geomeet.api.adapter.web.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for participant information.
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
}

