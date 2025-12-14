package com.geomeet.api.adapter.web.session.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session detail response.
 * Contains session information and list of participants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDetailResponse {
    private Long id;
    private String sessionId;
    private Long initiatorId;
    private String initiatorUsername;
    private String status;
    private String createdAt;
    private List<ParticipantInfo> participants;
    private Long participantCount;
}

