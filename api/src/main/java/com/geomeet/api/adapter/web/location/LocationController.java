package com.geomeet.api.adapter.web.location;

import com.geomeet.api.adapter.web.location.dto.UpdateLocationRequest;
import com.geomeet.api.adapter.web.location.dto.UpdateLocationResponse;
import com.geomeet.api.application.command.UpdateLocationCommand;
import com.geomeet.api.application.result.UpdateLocationResult;
import com.geomeet.api.application.usecase.UpdateLocationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web adapter (controller) for location management.
 * This is the entry point for location-related operations.
 */
@RestController
@RequestMapping("/api/sessions")
public class LocationController {

    private final UpdateLocationUseCase updateLocationUseCase;

    public LocationController(UpdateLocationUseCase updateLocationUseCase) {
        this.updateLocationUseCase = updateLocationUseCase;
    }

    /**
     * Update the current user's location in a session.
     * This endpoint allows participants to update their location.
     *
     * @param sessionId the session ID
     * @param request the location update request
     * @param authentication the authenticated user
     * @return location update response
     */
    @PutMapping("/{sessionId}/location")
    public ResponseEntity<UpdateLocationResponse> updateLocation(
        @PathVariable String sessionId,
        @Valid @RequestBody UpdateLocationRequest request,
        Authentication authentication
    ) {
        // Extract user ID from JWT token
        Long userId = (Long) authentication.getPrincipal();

        // Execute use case
        UpdateLocationCommand command = UpdateLocationCommand.of(
            sessionId,
            userId,
            request.getLatitude(),
            request.getLongitude(),
            request.getAccuracy()
        );
        UpdateLocationResult result = updateLocationUseCase.execute(command);

        // Convert result to response DTO
        UpdateLocationResponse response = UpdateLocationResponse.builder()
            .participantId(result.getParticipantId())
            .sessionId(result.getSessionId())
            .sessionIdString(result.getSessionIdString())
            .userId(result.getUserId())
            .latitude(result.getLatitude())
            .longitude(result.getLongitude())
            .accuracy(result.getAccuracy())
            .updatedAt(result.getUpdatedAt())
            .message(result.getMessage())
            .build();

        return ResponseEntity.ok(response);
    }
}

