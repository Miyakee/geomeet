package com.geomeet.api.application.usecase;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.geomeet.api.application.result.UpdateMeetingLocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BroadcastMeetingLocationUseCaseTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BroadcastMeetingLocationUseCase broadcastMeetingLocationUseCase;

    private UpdateMeetingLocationResult meetingLocationResult;

    @BeforeEach
    void setUp() {
        meetingLocationResult = UpdateMeetingLocationResult.builder()
            .sessionId(100L)
            .sessionIdString("test-session-id-123")
            .latitude(1.3521)
            .longitude(103.8198)
            .message("Meeting location updated successfully")
            .build();
    }

    @Test
    void shouldBroadcastMeetingLocationUpdate() {
        // When
        broadcastMeetingLocationUseCase.execute(meetingLocationResult);

        // Then
        String expectedDestination = "/topic/session/test-session-id-123/meeting-location";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(meetingLocationResult));
    }

    @Test
    void shouldBroadcastMeetingLocationUpdateWithDifferentSessionId() {
        // Given
        UpdateMeetingLocationResult differentResult = UpdateMeetingLocationResult.builder()
            .sessionId(200L)
            .sessionIdString("different-session-id-456")
            .latitude(1.2903)
            .longitude(103.8520)
            .message("Meeting location updated successfully")
            .build();

        // When
        broadcastMeetingLocationUseCase.execute(differentResult);

        // Then
        String expectedDestination = "/topic/session/different-session-id-456/meeting-location";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(differentResult));
    }
}

