package com.geomeet.api.adapter.web.session.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for join session request.
 */
@Data
public class JoinSessionRequest {
    @NotBlank(message = "Session ID is required")
    private String sessionId; // SessionId value (UUID string)
    
    @NotBlank(message = "Invite code is required")
    private String inviteCode; // Invitation code required to join
}

