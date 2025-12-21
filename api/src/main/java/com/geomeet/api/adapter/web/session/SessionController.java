package com.geomeet.api.adapter.web.session;

import com.geomeet.api.adapter.web.session.dto.CreateSessionRequest;
import com.geomeet.api.adapter.web.session.dto.CreateSessionResponse;
import com.geomeet.api.adapter.web.session.dto.EndSessionResponse;
import com.geomeet.api.adapter.web.session.dto.InviteLinkResponse;
import com.geomeet.api.adapter.web.session.dto.JoinSessionRequest;
import com.geomeet.api.adapter.web.session.dto.JoinSessionResponse;
import com.geomeet.api.adapter.web.session.dto.ParticipantInfo;
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
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web adapter (controller) for session management.
 * This is the entry point for session-related operations.
 */
@RestController
@RequestMapping("/api/sessions")
@AllArgsConstructor
public class SessionController {

  private final CreateSessionUseCase createSessionUseCase;
  private final JoinSessionUseCase joinSessionUseCase;
  private final GetSessionDetailsUseCase getSessionDetailsUseCase;
  private final GenerateInviteLinkUseCase generateInviteLinkUseCase;
  private final BroadcastSessionUpdateUseCase broadcastSessionUpdateUseCase;
  private final EndSessionUseCase endSessionUseCase;

  @PostMapping
  public ResponseEntity<CreateSessionResponse> createSession(
      @RequestBody CreateSessionRequest request,
      Authentication authentication
  ) {
    // Extract user ID from JWT token (set by JwtAuthenticationFilter)
    Long initiatorId = (Long) authentication.getPrincipal();

    CreateSessionCommand command = CreateSessionCommand.of(initiatorId);
    CreateSessionResult result = createSessionUseCase.execute(command);

    CreateSessionResponse response = CreateSessionResponse.create(result);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Generate an invitation link/code for a session.
   * Only the session initiator can generate the invite link.
   */
  @GetMapping("/{sessionId}/invite")
  public ResponseEntity<InviteLinkResponse> generateInviteLink(
      @PathVariable String sessionId,
      Authentication authentication
  ) {
    // Extract user ID from JWT token
    Long userId = (Long) authentication.getPrincipal();

    // Execute use case
    GenerateInviteLinkCommand command = GenerateInviteLinkCommand.of(sessionId, userId);
    GenerateInviteLinkResult result = generateInviteLinkUseCase.execute(command);

    // Convert result to response DTO
    InviteLinkResponse response = InviteLinkResponse.builder()
        .sessionId(result.getSessionId())
        .inviteLink(result.getInviteLink())
        .inviteCode(result.getInviteCode())
        .message("Invitation link generated successfully")
        .build();

    return ResponseEntity.ok(response);
  }

  /**
   * Join a session using invitation code (sessionId).
   */
  @PostMapping("/join")
  public ResponseEntity<JoinSessionResponse> joinSession(
      @Valid @RequestBody JoinSessionRequest request,
      Authentication authentication
  ) {
    // Extract user ID from JWT token
    Long userId = (Long) authentication.getPrincipal();

    // Execute use case
    JoinSessionCommand command = JoinSessionCommand.of(request.getSessionId(), userId);
    JoinSessionResult result = joinSessionUseCase.execute(command);

    // Convert result to response DTO
    JoinSessionResponse response = JoinSessionResponse.builder()
        .participantId(result.getParticipantId())
        .sessionId(result.getSessionId())
        .sessionIdString(result.getSessionIdString())
        .userId(result.getUserId())
        .joinedAt(result.getJoinedAt())
        .message(result.getMessage())
        .build();

    // Broadcast session update to all participants
    broadcastSessionUpdateUseCase.execute(result.getSessionIdString());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Get session details including participants.
   */
  @GetMapping("/{sessionId}")
  public ResponseEntity<SessionDetailResponse> getSessionDetails(
      @PathVariable String sessionId,
      Authentication authentication
  ) {
    // Extract user ID from JWT token
    Long userId = (Long) authentication.getPrincipal();

    // Execute use case
    GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionId, userId);
    GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

    // Convert result to response DTO
    SessionDetailResponse response = SessionDetailResponse.builder()
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
                .build())
            .collect(java.util.stream.Collectors.toList()))
        .participantCount(result.getParticipantCount())
        .meetingLocationLatitude(result.getMeetingLocationLatitude())
        .meetingLocationLongitude(result.getMeetingLocationLongitude())
        .participantLocations(result.getParticipantLocations() != null
            ? result.getParticipantLocations().stream()
                .map(location -> com.geomeet.api.adapter.web.session.dto.ParticipantLocationInfo.builder()
                    .participantId(location.getParticipantId())
                    .userId(location.getUserId())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .accuracy(location.getAccuracy())
                    .updatedAt(location.getUpdatedAt())
                    .build())
                .collect(java.util.stream.Collectors.toList())
            : null)
        .build();

    return ResponseEntity.ok(response);
  }

  /**
   * End a session.
   * Only the session initiator can end the session.
   */
  @DeleteMapping("/{sessionId}")
  public ResponseEntity<EndSessionResponse> endSession(
      @PathVariable String sessionId,
      Authentication authentication
  ) {
    // Extract user ID from JWT token
    Long userId = (Long) authentication.getPrincipal();

    // Execute use case
    EndSessionCommand command = EndSessionCommand.of(sessionId, userId);
    EndSessionResult result = endSessionUseCase.execute(command);

    // Convert result to response DTO
    EndSessionResponse response = EndSessionResponse.builder()
        .sessionId(result.getSessionId())
        .sessionIdString(result.getSessionIdString())
        .status(result.getStatus())
        .endedAt(result.getEndedAt())
        .message(result.getMessage())
        .build();

    return ResponseEntity.ok(response);
  }
}

