package com.geomeet.api.adapter.web.session;

import com.geomeet.api.adapter.web.session.dto.CreateSessionRequest;
import com.geomeet.api.adapter.web.session.dto.CreateSessionResponse;
import com.geomeet.api.application.command.CreateSessionCommand;
import com.geomeet.api.application.result.CreateSessionResult;
import com.geomeet.api.application.usecase.CreateSessionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    public SessionController(CreateSessionUseCase createSessionUseCase) {
        this.createSessionUseCase = createSessionUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateSessionResponse> createSession(
        @RequestBody CreateSessionRequest request,
        Authentication authentication
    ) {
        // Extract user ID from JWT token (set by JwtAuthenticationFilter)
        Long initiatorId = (Long) authentication.getPrincipal();

        CreateSessionCommand command = new CreateSessionCommand(initiatorId);
        CreateSessionResult result = createSessionUseCase.execute(command);

        CreateSessionResponse response = new CreateSessionResponse(
            result.getSessionId(),
            result.getSessionIdString(),
            result.getInitiatorId(),
            result.getStatus(),
            result.getCreatedAt(),
            "Session created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

