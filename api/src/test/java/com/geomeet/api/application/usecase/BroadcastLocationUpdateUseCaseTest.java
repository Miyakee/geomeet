package com.geomeet.api.application.usecase;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.geomeet.api.application.result.UpdateLocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BroadcastLocationUpdateUseCaseTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BroadcastLocationUpdateUseCase broadcastLocationUpdateUseCase;

    private UpdateLocationResult updateLocationResult;

    @BeforeEach
    void setUp() {
        updateLocationResult = UpdateLocationResult.builder()
            .participantId(1L)
            .sessionId(100L)
            .sessionIdString("test-session-id-123")
            .userId(1L)
            .latitude(1.3521)
            .longitude(103.8198)
            .accuracy(10.0)
            .updatedAt("2024-01-01T00:00:00")
            .message("Location updated successfully")
            .build();
    }

    @Test
    void shouldBroadcastLocationUpdate() {
        // When
        broadcastLocationUpdateUseCase.execute(updateLocationResult);

        // Then
        String expectedDestination = "/topic/session/test-session-id-123/locations";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(updateLocationResult));
    }

    @Test
    void shouldBroadcastLocationUpdateWithDifferentSessionId() {
        // Given
        UpdateLocationResult differentResult = UpdateLocationResult.builder()
            .participantId(2L)
            .sessionId(200L)
            .sessionIdString("different-session-id-456")
            .userId(2L)
            .latitude(1.2903)
            .longitude(103.8520)
            .accuracy(15.0)
            .updatedAt("2024-01-01T01:00:00")
            .message("Location updated successfully")
            .build();

        // When
        broadcastLocationUpdateUseCase.execute(differentResult);

        // Then
        String expectedDestination = "/topic/session/different-session-id-456/locations";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(differentResult));
    }
}

