package com.geomeet.api.adapter.web.session;

import com.geomeet.api.adapter.web.session.dto.CreateSessionRequest;
import com.geomeet.api.adapter.web.session.dto.CreateSessionResponse;
import com.geomeet.api.adapter.web.session.dto.EndSessionResponse;
import com.geomeet.api.adapter.web.session.dto.InviteLinkResponse;
import com.geomeet.api.adapter.web.session.dto.JoinSessionRequest;
import com.geomeet.api.adapter.web.session.dto.JoinSessionResponse;
import com.geomeet.api.adapter.web.session.dto.SessionDetailResponse;
import com.geomeet.api.application.command.CreateSessionCommand;
import com.geomeet.api.application.command.EndSessionCommand;
import com.geomeet.api.application.command.GenerateInviteLinkCommand;
import com.geomeet.api.application.command.GetSessionDetailsCommand;
import com.geomeet.api.application.command.JoinSessionCommand;
import com.geomeet.api.application.result.CreateSessionResult;
import com.geomeet.api.application.result.EndSessionResult;
import com.geomeet.api.application.result.GenerateInviteLinkResult;
import com.geomeet.api.application.result.GetSessionDetailsResult;
import com.geomeet.api.application.result.JoinSessionResult;
import com.geomeet.api.application.usecase.session.BroadcastSessionUpdateUseCase;
import com.geomeet.api.application.usecase.session.CreateSessionUseCase;
import com.geomeet.api.application.usecase.session.EndSessionUseCase;
import com.geomeet.api.application.usecase.session.GenerateInviteLinkUseCase;
import com.geomeet.api.application.usecase.session.GetSessionDetailsUseCase;
import com.geomeet.api.application.usecase.session.JoinSessionUseCase;
import com.geomeet.api.adapter.web.util.AuthenticationUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.geomeet.api.adapter.web.util.ResponseUtil.created;
import static com.geomeet.api.adapter.web.util.ResponseUtil.ok;

/**
 * Web adapter (controller) for session management.
 * This is the entry point for session-related operations.
 */
@RestController
@RequestMapping("/api/sessions")
@AllArgsConstructor
@Tag(name = "Sessions", description = "Session management APIs for creating, joining, and managing meeting sessions")
@SecurityRequirement(name = "Bearer Authentication")
public class SessionController {

  private final CreateSessionUseCase createSessionUseCase;
  private final JoinSessionUseCase joinSessionUseCase;
  private final GetSessionDetailsUseCase getSessionDetailsUseCase;
  private final GenerateInviteLinkUseCase generateInviteLinkUseCase;
  private final BroadcastSessionUpdateUseCase broadcastSessionUpdateUseCase;
  private final EndSessionUseCase endSessionUseCase;

  @Operation(
      summary = "Create a new session",
      description = "Create a new meeting session. The authenticated user becomes the session initiator."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Session created successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @PostMapping
  public ResponseEntity<CreateSessionResponse> createSession(
      @RequestBody CreateSessionRequest request,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long initiatorId = AuthenticationUtil.getUserId(authentication);

    CreateSessionCommand command = CreateSessionCommand.of(initiatorId);
    CreateSessionResult result = createSessionUseCase.execute(command);

    return created(CreateSessionResponse.create(result));
  }

  @Operation(
      summary = "Generate invitation link",
      description = "Generate an invitation link and code for a session. "
          + "Only the session initiator can generate the invite link."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Invitation link generated successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Only the session initiator can generate invite links"),
      @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @GetMapping("/{sessionId}/invite")
  public ResponseEntity<InviteLinkResponse> generateInviteLink(
      @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    GenerateInviteLinkCommand command = GenerateInviteLinkCommand.of(sessionId, userId);
    GenerateInviteLinkResult result = generateInviteLinkUseCase.execute(command);

    return ok(InviteLinkResponse.from(result));
  }

  @Operation(
      summary = "Join a session",
      description = "Join a session using session ID and invitation code."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Successfully joined the session"),
      @ApiResponse(responseCode = "400", description = "Invalid request data or invalid invite code"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @PostMapping("/join")
  public ResponseEntity<JoinSessionResponse> joinSession(
      @Valid @RequestBody JoinSessionRequest request,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    JoinSessionCommand command = JoinSessionCommand.of(request.getSessionId(), request.getInviteCode(), userId);
    JoinSessionResult result = joinSessionUseCase.execute(command);

    broadcastSessionUpdateUseCase.execute(result.getSessionIdString());

    return created(JoinSessionResponse.from(result));
  }

  @Operation(
      summary = "Get session details",
      description = "Get detailed information about a session including participants and their locations."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Session details retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied - user is not a participant or initiator"),
      @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @GetMapping("/{sessionId}")
  public ResponseEntity<SessionDetailResponse> getSessionDetails(
      @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionId, userId);
    GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

    return ok(SessionDetailResponse.from(result));
  }

  @Operation(
      summary = "End a session",
      description = "End a session. Only the session initiator can end the session."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Session ended successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Only the session initiator can end the session"),
      @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @DeleteMapping("/{sessionId}")
  public ResponseEntity<EndSessionResponse> endSession(
      @Parameter(description = "Session ID", required = true) @PathVariable String sessionId,
      @Parameter(hidden = true) Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    EndSessionCommand command = EndSessionCommand.of(sessionId, userId);
    EndSessionResult result = endSessionUseCase.execute(command);

    return ok(EndSessionResponse.from(result));
  }
}

