package com.geomeet.api.adapter.web.session.dto;

import com.geomeet.api.application.result.EndSessionResult;
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

    /**
     * Creates an EndSessionResponse from an EndSessionResult.
     */
    public static EndSessionResponse from(EndSessionResult result) {
        return EndSessionResponse.builder()
            .sessionId(result.getSessionId())
            .sessionIdString(result.getSessionIdString())
            .status(result.getStatus())
            .endedAt(result.getEndedAt())
            .message(result.getMessage())
            .build();
    }
}

