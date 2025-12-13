package com.geomeet.api.adapter.web.session.dto;

import lombok.Data;

/**
 * DTO for join session request.
 */
@Data
public class JoinSessionRequest {
    private String sessionId; // SessionId value (UUID string)
}

