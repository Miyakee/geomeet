package com.geomeet.api.adapter.web.session.dto;

import com.geomeet.api.application.result.GenerateInviteLinkResult;
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

    /**
     * Creates an InviteLinkResponse from a GenerateInviteLinkResult.
     */
    public static InviteLinkResponse from(GenerateInviteLinkResult result) {
        return InviteLinkResponse.builder()
            .sessionId(result.getSessionId())
            .inviteLink(result.getInviteLink())
            .inviteCode(result.getInviteCode())
            .message("Invitation link generated successfully")
            .build();
    }
}

