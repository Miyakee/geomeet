package com.geomeet.api.adapter.web.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for create session response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionResponse {

    private Long id;
    private String sessionId;
    private Long initiatorId;
    private String status;
    private String createdAt;
    private String message;
}

