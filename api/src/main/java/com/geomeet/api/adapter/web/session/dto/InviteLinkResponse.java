package com.geomeet.api.adapter.web.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for invite link response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteLinkResponse {
    private String sessionId;
    private String inviteLink;
    private String inviteCode;
    private String message;
}

