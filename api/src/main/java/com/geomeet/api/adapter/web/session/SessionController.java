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

import static com.geomeet.api.adapter.web.util.ResponseUtil.created;
import static com.geomeet.api.adapter.web.util.ResponseUtil.ok;

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
    Long initiatorId = AuthenticationUtil.getUserId(authentication);

    CreateSessionCommand command = CreateSessionCommand.of(initiatorId);
    CreateSessionResult result = createSessionUseCase.execute(command);

    return created(CreateSessionResponse.create(result));
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
    Long userId = AuthenticationUtil.getUserId(authentication);

    GenerateInviteLinkCommand command = GenerateInviteLinkCommand.of(sessionId, userId);
    GenerateInviteLinkResult result = generateInviteLinkUseCase.execute(command);

    return ok(InviteLinkResponse.from(result));
  }

  /**
   * Join a session using invitation code (sessionId).
   */
  @PostMapping("/join")
  public ResponseEntity<JoinSessionResponse> joinSession(
      @Valid @RequestBody JoinSessionRequest request,
      Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    JoinSessionCommand command = JoinSessionCommand.of(request.getSessionId(), request.getInviteCode(), userId);
    JoinSessionResult result = joinSessionUseCase.execute(command);

    broadcastSessionUpdateUseCase.execute(result.getSessionIdString());

    return created(JoinSessionResponse.from(result));
  }

  /**
   * Get session details including participants.
   */
  @GetMapping("/{sessionId}")
  public ResponseEntity<SessionDetailResponse> getSessionDetails(
      @PathVariable String sessionId,
      Authentication authentication
  ) {
    Long userId = AuthenticationUtil.getUserId(authentication);

    GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionId, userId);
    GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

    return ok(SessionDetailResponse.from(result));
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
    Long userId = AuthenticationUtil.getUserId(authentication);

    EndSessionCommand command = EndSessionCommand.of(sessionId, userId);
    EndSessionResult result = endSessionUseCase.execute(command);

    return ok(EndSessionResponse.from(result));
  }
}

