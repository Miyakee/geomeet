package com.geomeet.api.application.usecase;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.geomeet.api.application.result.CalculateOptimalLocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BroadcastOptimalLocationUseCaseTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BroadcastOptimalLocationUseCase broadcastOptimalLocationUseCase;

    private CalculateOptimalLocationResult optimalLocationResult;

    @BeforeEach
    void setUp() {
        optimalLocationResult = CalculateOptimalLocationResult.builder()
            .sessionId(100L)
            .sessionIdString("test-session-id-123")
            .optimalLatitude(1.3521)
            .optimalLongitude(103.8198)
            .totalTravelDistance(15.5)
            .participantCount(3)
            .message("Optimal location calculated successfully")
            .build();
    }

    @Test
    void shouldBroadcastOptimalLocationUpdate() {
        // When
        broadcastOptimalLocationUseCase.execute(optimalLocationResult);

        // Then
        String expectedDestination = "/topic/session/test-session-id-123/optimal-location";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(optimalLocationResult));
    }

    @Test
    void shouldBroadcastOptimalLocationUpdateWithDifferentSessionId() {
        // Given
        CalculateOptimalLocationResult differentResult = CalculateOptimalLocationResult.builder()
            .sessionId(200L)
            .sessionIdString("different-session-id-456")
            .optimalLatitude(1.2903)
            .optimalLongitude(103.8520)
            .totalTravelDistance(20.0)
            .participantCount(5)
            .message("Optimal location calculated successfully")
            .build();

        // When
        broadcastOptimalLocationUseCase.execute(differentResult);

        // Then
        String expectedDestination = "/topic/session/different-session-id-456/optimal-location";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(differentResult));
    }
}

