package com.geomeet.api.adapter.web.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for join session response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinSessionResponse {
    private Long participantId;
    private Long sessionId;
    private String sessionIdString;
    private Long userId;
    private String joinedAt;
    private String message;
}

