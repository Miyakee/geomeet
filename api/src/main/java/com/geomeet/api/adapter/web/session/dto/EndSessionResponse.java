package com.geomeet.api.adapter.web.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for ending a session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndSessionResponse {
    private Long sessionId;
    private String sessionIdString;
    private String status;
    private String endedAt;
    private String message;
}

