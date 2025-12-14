package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for generate invite link use case.
 * Contains the invitation link and code.
 */
@Getter
@Builder
public class GenerateInviteLinkResult {

    private final String sessionId;
    private final String inviteLink;
    private final String inviteCode;
}

