package com.geomeet.api.adapter.web.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock
    private CreateSessionUseCase createSessionUseCase;

    @Mock
    private JoinSessionUseCase joinSessionUseCase;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private Authentication authentication;

    private SessionController sessionController;

    private Long initiatorId;
    private Long sessionId;
    private String sessionIdString;

    @BeforeEach
    void setUp() {
        sessionController = new SessionController(createSessionUseCase, joinSessionUseCase, sessionRepository);
        initiatorId = 1L;
        sessionId = 100L;
        sessionIdString = "test-session-id-123";
    }

    @Test
    void shouldCreateSessionSuccessfully() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest();
        when(authentication.getPrincipal()).thenReturn(initiatorId);

        CreateSessionResult result = CreateSessionResult.builder()
            .sessionId(sessionId)
            .sessionIdString(sessionIdString)
            .initiatorId(initiatorId)
            .status(SessionStatus.ACTIVE.getValue())
            .createdAt("2024-01-01T00:00:00")
            .build();

        when(createSessionUseCase.execute(any(CreateSessionCommand.class))).thenReturn(result);

        // When
        ResponseEntity<CreateSessionResponse> response = sessionController.createSession(
            request,
            authentication
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        CreateSessionResponse responseBody = response.getBody();
        assertEquals(sessionId, responseBody.getId());
        assertEquals(sessionIdString, responseBody.getSessionId());
        assertEquals(initiatorId, responseBody.getInitiatorId());
        assertEquals(SessionStatus.ACTIVE.getValue(), responseBody.getStatus());
        assertEquals("Session created successfully", responseBody.getMessage());

        verify(authentication).getPrincipal();
        verify(createSessionUseCase).execute(any(CreateSessionCommand.class));
    }

    @Test
    void shouldGenerateInviteLinkSuccessfully() {
        // Given
        Long userId = 1L;
        SessionId sessionIdVO = SessionId.fromString(sessionIdString);
        Session session = Session.reconstruct(
            sessionId,
            sessionIdVO,
            userId,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        when(authentication.getPrincipal()).thenReturn(userId);
        when(sessionRepository.findBySessionId(sessionIdVO)).thenReturn(Optional.of(session));

        // When
        ResponseEntity<InviteLinkResponse> response = sessionController.generateInviteLink(
            sessionIdString,
            authentication
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        InviteLinkResponse responseBody = response.getBody();
        assertEquals(sessionIdString, responseBody.getSessionId());
        assertEquals("/join?sessionId=" + sessionIdString, responseBody.getInviteLink());
        assertEquals(sessionIdString, responseBody.getInviteCode());
        assertEquals("Invitation link generated successfully", responseBody.getMessage());

        verify(authentication).getPrincipal();
        verify(sessionRepository).findBySessionId(sessionIdVO);
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotInitiator() {
        // Given
        Long userId = 1L;
        Long otherUserId = 2L;
        SessionId sessionIdVO = SessionId.fromString(sessionIdString);
        Session session = Session.reconstruct(
            sessionId,
            sessionIdVO,
            otherUserId, // Different initiator
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        when(authentication.getPrincipal()).thenReturn(userId);
        when(sessionRepository.findBySessionId(sessionIdVO)).thenReturn(Optional.of(session));

        // When
        ResponseEntity<InviteLinkResponse> response = sessionController.generateInviteLink(
            sessionIdString,
            authentication
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(authentication).getPrincipal();
        verify(sessionRepository).findBySessionId(sessionIdVO);
    }

    @Test
    void shouldJoinSessionSuccessfully() {
        // Given
        Long userId = 2L;
        Long participantId = 200L;
        JoinSessionRequest request = new JoinSessionRequest();
        request.setSessionId(sessionIdString);

        JoinSessionResult result = JoinSessionResult.builder()
            .participantId(participantId)
            .sessionId(sessionId)
            .sessionIdString(sessionIdString)
            .userId(userId)
            .joinedAt("2024-01-01T00:00:00")
            .message("Successfully joined session")
            .build();

        when(authentication.getPrincipal()).thenReturn(userId);
        when(joinSessionUseCase.execute(any(JoinSessionCommand.class))).thenReturn(result);

        // When
        ResponseEntity<?> response = sessionController.joinSession(request, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        JoinSessionResponse responseBody = (JoinSessionResponse) response.getBody();
        assertEquals(participantId, responseBody.getParticipantId());
        assertEquals(sessionId, responseBody.getSessionId());
        assertEquals(sessionIdString, responseBody.getSessionIdString());
        assertEquals(userId, responseBody.getUserId());
        assertEquals("Successfully joined session", responseBody.getMessage());

        verify(authentication).getPrincipal();
        verify(joinSessionUseCase).execute(any(JoinSessionCommand.class));
    }
}

