package com.geomeet.api.adapter.web.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.adapter.web.location.dto.CalculateOptimalLocationResponse;
import com.geomeet.api.adapter.web.location.dto.UpdateLocationRequest;
import com.geomeet.api.adapter.web.location.dto.UpdateLocationResponse;
import com.geomeet.api.application.command.CalculateOptimalLocationCommand;
import com.geomeet.api.application.command.UpdateLocationCommand;
import com.geomeet.api.application.result.CalculateOptimalLocationResult;
import com.geomeet.api.application.result.UpdateLocationResult;
import com.geomeet.api.application.usecase.CalculateOptimalLocationUseCase;
import com.geomeet.api.application.usecase.UpdateLocationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private UpdateLocationUseCase updateLocationUseCase;

    @Mock
    private CalculateOptimalLocationUseCase calculateOptimalLocationUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LocationController locationController;

    private Long userId;
    private String sessionId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        sessionId = "test-session-id-123";
    }

    @Test
    void shouldUpdateLocationSuccessfully() {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest();
        request.setLatitude(1.3521);
        request.setLongitude(103.8198);
        request.setAccuracy(10.0);

        UpdateLocationResult result = UpdateLocationResult.builder()
            .participantId(1L)
            .sessionId(100L)
            .sessionIdString(sessionId)
            .userId(userId)
            .latitude(1.3521)
            .longitude(103.8198)
            .accuracy(10.0)
            .updatedAt("2024-01-01T00:00:00")
            .message("Location updated successfully")
            .build();

        when(authentication.getPrincipal()).thenReturn(userId);
        when(updateLocationUseCase.execute(any(UpdateLocationCommand.class)))
            .thenReturn(result);

        // When
        ResponseEntity<UpdateLocationResponse> response = locationController.updateLocation(
            sessionId, request, authentication
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(result.getParticipantId(), response.getBody().getParticipantId());
        assertEquals(result.getSessionId(), response.getBody().getSessionId());
        assertEquals(result.getSessionIdString(), response.getBody().getSessionIdString());
        assertEquals(result.getUserId(), response.getBody().getUserId());
        assertEquals(result.getLatitude(), response.getBody().getLatitude());
        assertEquals(result.getLongitude(), response.getBody().getLongitude());
        assertEquals(result.getAccuracy(), response.getBody().getAccuracy());
        assertEquals(result.getUpdatedAt(), response.getBody().getUpdatedAt());
        assertEquals(result.getMessage(), response.getBody().getMessage());

        verify(updateLocationUseCase).execute(any(UpdateLocationCommand.class));
    }

    @Test
    void shouldCalculateOptimalLocationSuccessfully() {
        // Given
        CalculateOptimalLocationResult result = CalculateOptimalLocationResult.builder()
            .sessionId(100L)
            .sessionIdString(sessionId)
            .optimalLatitude(1.3521)
            .optimalLongitude(103.8198)
            .totalTravelDistance(15.5)
            .participantCount(3)
            .message("Optimal location calculated successfully")
            .build();

        when(authentication.getPrincipal()).thenReturn(userId);
        when(calculateOptimalLocationUseCase.execute(any(CalculateOptimalLocationCommand.class)))
            .thenReturn(result);

        // When
        ResponseEntity<CalculateOptimalLocationResponse> response = locationController.calculateOptimalLocation(
            sessionId, authentication
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(result.getSessionId(), response.getBody().getSessionId());
        assertEquals(result.getSessionIdString(), response.getBody().getSessionIdString());
        assertEquals(result.getOptimalLatitude(), response.getBody().getOptimalLatitude());
        assertEquals(result.getOptimalLongitude(), response.getBody().getOptimalLongitude());
        assertEquals(result.getTotalTravelDistance(), response.getBody().getTotalTravelDistance());
        assertEquals(result.getParticipantCount(), response.getBody().getParticipantCount());
        assertEquals(result.getMessage(), response.getBody().getMessage());

        verify(calculateOptimalLocationUseCase).execute(any(CalculateOptimalLocationCommand.class));
    }

    @Test
    void shouldUpdateLocationWithoutAccuracy() {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest();
        request.setLatitude(1.3521);
        request.setLongitude(103.8198);
        // accuracy is null

        UpdateLocationResult result = UpdateLocationResult.builder()
            .participantId(1L)
            .sessionId(100L)
            .sessionIdString(sessionId)
            .userId(userId)
            .latitude(1.3521)
            .longitude(103.8198)
            .accuracy(null)
            .updatedAt("2024-01-01T00:00:00")
            .message("Location updated successfully")
            .build();

        when(authentication.getPrincipal()).thenReturn(userId);
        when(updateLocationUseCase.execute(any(UpdateLocationCommand.class)))
            .thenReturn(result);

        // When
        ResponseEntity<UpdateLocationResponse> response = locationController.updateLocation(
            sessionId, request, authentication
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(result.getAccuracy(), response.getBody().getAccuracy());
    }
}

