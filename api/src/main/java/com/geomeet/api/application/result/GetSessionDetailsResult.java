package com.geomeet.api.application.result;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Result object for get session details use case.
 * Contains session information and list of participants.
 */
@Getter
@Builder
public class GetSessionDetailsResult {

    private final Long id;
    private final String sessionId;
    private final Long initiatorId;
    private final String initiatorUsername;
    private final String status;
    private final String createdAt;
    private final List<ParticipantInfo> participants;
    private final Long participantCount;
    private final Double meetingLocationLatitude;
    private final Double meetingLocationLongitude;

    /**
     * Participant information within the result.
     */
    @Getter
    @Builder
    public static class ParticipantInfo {
        private final Long participantId;
        private final Long userId;
        private final String username;
        private final String email;
        private final String joinedAt;
    }
}

