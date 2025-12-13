package com.geomeet.api.adapter.web.session;

import com.geomeet.api.adapter.web.session.dto.CreateSessionRequest;
import com.geomeet.api.adapter.web.session.dto.CreateSessionResponse;
import com.geomeet.api.adapter.web.session.dto.InviteLinkResponse;
import com.geomeet.api.adapter.web.session.dto.JoinSessionRequest;
import com.geomeet.api.adapter.web.session.dto.JoinSessionResponse;
import com.geomeet.api.application.command.CreateSessionCommand;
import com.geomeet.api.application.command.JoinSessionCommand;
import com.geomeet.api.application.result.CreateSessionResult;
import com.geomeet.api.application.result.JoinSessionResult;
import com.geomeet.api.application.usecase.CreateSessionUseCase;
import com.geomeet.api.application.usecase.JoinSessionUseCase;
import com.geomeet.api.application.usecase.SessionRepository;
import com.geomeet.api.domain.valueobject.SessionId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
public class SessionController {

    private final CreateSessionUseCase createSessionUseCase;
    private final JoinSessionUseCase joinSessionUseCase;
    private final SessionRepository sessionRepository;

    public SessionController(
        CreateSessionUseCase createSessionUseCase,
        JoinSessionUseCase joinSessionUseCase,
        SessionRepository sessionRepository
    ) {
        this.createSessionUseCase = createSessionUseCase;
        this.joinSessionUseCase = joinSessionUseCase;
        this.sessionRepository = sessionRepository;
    }

    @PostMapping
    public ResponseEntity<CreateSessionResponse> createSession(
        @RequestBody CreateSessionRequest request,
        Authentication authentication
    ) {
        // Extract user ID from JWT token (set by JwtAuthenticationFilter)
        Long initiatorId = (Long) authentication.getPrincipal();

        CreateSessionCommand command = CreateSessionCommand.of(initiatorId);
        CreateSessionResult result = createSessionUseCase.execute(command);

        CreateSessionResponse response = CreateSessionResponse.builder()
            .id(result.getSessionId())
            .sessionId(result.getSessionIdString())
            .initiatorId(result.getInitiatorId())
            .status(result.getStatus())
            .createdAt(result.getCreatedAt())
            .message("Session created successfully")
            .build();

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

        // Find session
        SessionId sessionIdVO = SessionId.fromString(sessionId);
        com.geomeet.api.domain.entity.Session session = sessionRepository.findBySessionId(sessionIdVO)
            .orElseThrow(() -> new RuntimeException("Session not found"));

        // Verify user is the initiator
        if (!session.getInitiatorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Generate invite link (using sessionId as the code)
        String inviteLink = "/join?sessionId=" + sessionId;
        String inviteCode = sessionId;

        InviteLinkResponse response = InviteLinkResponse.builder()
            .sessionId(sessionId)
            .inviteLink(inviteLink)
            .inviteCode(inviteCode)
            .message("Invitation link generated successfully")
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Join a session using invitation code (sessionId).
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinSession(
        @Valid @RequestBody JoinSessionRequest request,
        Authentication authentication
    ) {
        try {
            // Extract user ID from JWT token
            Long userId = (Long) authentication.getPrincipal();

            JoinSessionCommand command = JoinSessionCommand.of(request.getSessionId(), userId);
            JoinSessionResult result = joinSessionUseCase.execute(command);

            JoinSessionResponse response = JoinSessionResponse.builder()
                .participantId(result.getParticipantId())
                .sessionId(result.getSessionId())
                .sessionIdString(result.getSessionIdString())
                .userId(result.getUserId())
                .joinedAt(result.getJoinedAt())
                .message(result.getMessage())
                .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // Exception will be handled by GlobalExceptionHandler
            throw e;
        }
    }
}

