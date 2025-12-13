package com.geomeet.api.adapter.web.session.dto;

import lombok.Data;

/**
 * DTO for create session request.
 * Currently empty as initiator ID comes from JWT token.
 */
@Data
public class CreateSessionRequest {
    // Empty - initiator ID is extracted from JWT token
}

