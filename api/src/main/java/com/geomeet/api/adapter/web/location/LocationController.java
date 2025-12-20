package com.geomeet.api.adapter.web.location;

import com.geomeet.api.adapter.web.location.dto.CalculateOptimalLocationResponse;
import com.geomeet.api.adapter.web.location.dto.UpdateLocationRequest;
import com.geomeet.api.adapter.web.location.dto.UpdateLocationResponse;
import com.geomeet.api.adapter.web.location.dto.UpdateMeetingLocationRequest;
import com.geomeet.api.adapter.web.location.dto.UpdateMeetingLocationResponse;
import com.geomeet.api.application.command.CalculateOptimalLocationCommand;
import com.geomeet.api.application.command.UpdateLocationCommand;
import com.geomeet.api.application.command.UpdateMeetingLocationCommand;
import com.geomeet.api.application.result.CalculateOptimalLocationResult;
import com.geomeet.api.application.result.UpdateLocationResult;
import com.geomeet.api.application.result.UpdateMeetingLocationResult;
import com.geomeet.api.application.usecase.location.CalculateOptimalLocationUseCase;
import com.geomeet.api.application.usecase.location.UpdateLocationUseCase;
import com.geomeet.api.application.usecase.location.UpdateMeetingLocationUseCase;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@AllArgsConstructor
public class LocationController {

  private final UpdateLocationUseCase updateLocationUseCase;
  private final CalculateOptimalLocationUseCase calculateOptimalLocationUseCase;
  private final UpdateMeetingLocationUseCase updateMeetingLocationUseCase;


  /**
   * Update the current user's location in a session.
   * This endpoint allows participants to update their location.
   *
   * @param sessionId      the session ID
   * @param request        the location update request
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
    UpdateLocationResponse response = UpdateLocationResponse.create(result);

    return ResponseEntity.ok(response);
  }

  /**
   * Calculate the optimal meeting location for a session.
   * This endpoint calculates the geometric center of all participant locations.
   *
   * @param sessionId      the session ID
   * @param authentication the authenticated user
   * @return optimal location response
   */
  @PostMapping("/{sessionId}/optimal-location")
  public ResponseEntity<CalculateOptimalLocationResponse> calculateOptimalLocation(
      @PathVariable String sessionId,
      Authentication authentication
  ) {
    // Extract user ID from JWT token
    Long userId = (Long) authentication.getPrincipal();

    // Execute use case
    CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(sessionId, userId);
    CalculateOptimalLocationResult result = calculateOptimalLocationUseCase.execute(command);

    // Convert result to response DTO
    CalculateOptimalLocationResponse response = CalculateOptimalLocationResponse.create(result);

    return ResponseEntity.ok(response);
  }

  /**
   * Update the meeting location for a session.
   * Only the session initiator can update the meeting location.
   *
   * @param sessionId      the session ID
   * @param request        the meeting location update request
   * @param authentication the authenticated user
   * @return meeting location update response
   */
  @PutMapping("/{sessionId}/meeting-location")
  public ResponseEntity<UpdateMeetingLocationResponse> updateMeetingLocation(
      @PathVariable String sessionId,
      @Valid @RequestBody UpdateMeetingLocationRequest request,
      Authentication authentication
  ) {
    // Extract user ID from JWT token
    Long userId = (Long) authentication.getPrincipal();

    // Execute use case
    UpdateMeetingLocationCommand command = UpdateMeetingLocationCommand.of(
        sessionId,
        userId,
        request.getLatitude(),
        request.getLongitude()
    );
    UpdateMeetingLocationResult result = updateMeetingLocationUseCase.execute(command);

    // Convert result to response DTO
    UpdateMeetingLocationResponse response = UpdateMeetingLocationResponse.create(result);

    return ResponseEntity.ok(response);
  }
}

