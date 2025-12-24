package com.geomeet.api.adapter.web.session.dto;

import com.geomeet.api.application.result.JoinSessionResult;
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

    /**
     * Creates a JoinSessionResponse from a JoinSessionResult.
     */
    public static JoinSessionResponse from(JoinSessionResult result) {
        return JoinSessionResponse.builder()
            .participantId(result.getParticipantId())
            .sessionId(result.getSessionId())
            .sessionIdString(result.getSessionIdString())
            .userId(result.getUserId())
            .joinedAt(result.getJoinedAt())
            .message(result.getMessage())
            .build();
    }
}

