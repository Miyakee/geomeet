package com.geomeet.api.adapter.web.session.dto;

import com.geomeet.api.application.result.GetSessionDetailsResult;
import java.util.List;
import java.util.stream.Collectors;
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
    private Double meetingLocationLatitude;
    private Double meetingLocationLongitude;

    /**
     * Creates a SessionDetailResponse from a GetSessionDetailsResult.
     */
    public static SessionDetailResponse from(GetSessionDetailsResult result) {
        return SessionDetailResponse.builder()
            .id(result.getId())
            .sessionId(result.getSessionId())
            .initiatorId(result.getInitiatorId())
            .initiatorUsername(result.getInitiatorUsername())
            .status(result.getStatus())
            .createdAt(result.getCreatedAt())
            .participants(result.getParticipants().stream()
                .map(participant -> ParticipantInfo.builder()
                    .participantId(participant.getParticipantId())
                    .userId(participant.getUserId())
                    .username(participant.getUsername())
                    .email(participant.getEmail())
                    .joinedAt(participant.getJoinedAt())
                    .latitude(participant.getLatitude())
                    .longitude(participant.getLongitude())
                    .accuracy(participant.getAccuracy())
                    .locationUpdatedAt(participant.getLocationUpdatedAt())
                    .build())
                .collect(Collectors.toList()))
            .participantCount(result.getParticipantCount())
            .meetingLocationLatitude(result.getMeetingLocationLatitude())
            .meetingLocationLongitude(result.getMeetingLocationLongitude())
            .build();
    }
}

