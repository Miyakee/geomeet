package com.geomeet.api.adapter.web.session.dto;

import com.geomeet.api.adapter.web.location.dto.CalculateOptimalLocationResponse;
import com.geomeet.api.application.result.CreateSessionResult;
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

    public static CreateSessionResponse create(CreateSessionResult result){
        return CreateSessionResponse.builder()
            .id(result.getSessionId())
            .sessionId(result.getSessionIdString())
            .initiatorId(result.getInitiatorId())
            .status(result.getStatus())
            .createdAt(result.getCreatedAt())
            .message("Session created successfully")
            .build();
    }
}

