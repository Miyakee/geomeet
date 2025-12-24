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
import com.geomeet.api.adapter.web.util.AuthenticationUtil;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.geomeet.api.adapter.web.util.ResponseUtil.ok;

/**
 * Web adapter (controller) for location management.
 * This is the entry point for location-related operations.
 */
@RestController
@RequestMapping("/api/sessions")
@AllArgsConstructor
@Tag(name = "Locations", description = "Location management APIs for updating and calculating meeting locations")
@SecurityRequirement(name = "Bearer Authentication")
public class LocationController {

  private final UpdateLocationUseCase updateLocationUseCase;
  private final CalculateOptimalLocationUseCase calculateOptimalLocationUseCase;
  private final UpdateMeetingLocationUseCase updateMeetingLocationUseCase;


  @Operation(
      summary = "Update participant location",
      description = "Update the current user's location in a session. This endpoint allows participants to share their location."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Location updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid location data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @PutMapping("/{sessionId}/location")
  public ResponseEntity<UpdateLocationResponse> updateLocation(
      @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
      @Valid @RequestBody UpdateLocationRequest request,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    UpdateLocationCommand command = UpdateLocationCommand.of(
        sessionId,
        userId,
        request.getLatitude(),
        request.getLongitude(),
        request.getAccuracy()
    );
    UpdateLocationResult result = updateLocationUseCase.execute(command);

    return ok(UpdateLocationResponse.create(result));
  }

  @Operation(
      summary = "Calculate optimal meeting location",
      description = "Calculate the optimal meeting location (geometric center) based on all participant locations."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Optimal location calculated successfully"),
      @ApiResponse(responseCode = "400", description = "Cannot calculate - at least one participant must share location"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @PostMapping("/{sessionId}/optimal-location")
  public ResponseEntity<CalculateOptimalLocationResponse> calculateOptimalLocation(
      @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    CalculateOptimalLocationCommand command = CalculateOptimalLocationCommand.of(sessionId, userId);
    CalculateOptimalLocationResult result = calculateOptimalLocationUseCase.execute(command);

    return ok(CalculateOptimalLocationResponse.create(result));
  }

  @Operation(
      summary = "Update meeting location",
      description = "Update the meeting location for a session. Only the session initiator can update the meeting location."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Meeting location updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid location data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Only the session initiator can update meeting location"),
      @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @PutMapping("/{sessionId}/meeting-location")
  public ResponseEntity<UpdateMeetingLocationResponse> updateMeetingLocation(
      @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
      @Valid @RequestBody UpdateMeetingLocationRequest request,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    UpdateMeetingLocationCommand command = UpdateMeetingLocationCommand.of(
        sessionId,
        userId,
        request.getLatitude(),
        request.getLongitude()
    );
    UpdateMeetingLocationResult result = updateMeetingLocationUseCase.execute(command);

    return ok(UpdateMeetingLocationResponse.create(result));
  }
}

