package com.geomeet.api.adapter.web.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.geomeet.api.domain.valueobject.SessionStatus;
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
    private GetSessionDetailsUseCase getSessionDetailsUseCase;

    @Mock
    private GenerateInviteLinkUseCase generateInviteLinkUseCase;

    @Mock
    private BroadcastSessionUpdateUseCase broadcastSessionUpdateUseCase;

    @Mock
    private EndSessionUseCase endSessionUseCase;

    @Mock
    private Authentication authentication;

    private SessionController sessionController;

    private Long initiatorId;
    private Long sessionId;
    private String sessionIdString;

    @BeforeEach
    void setUp() {
        sessionController = new SessionController(
            createSessionUseCase,
            joinSessionUseCase,
            getSessionDetailsUseCase,
            generateInviteLinkUseCase,
            broadcastSessionUpdateUseCase,
            endSessionUseCase
        );
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
        GenerateInviteLinkResult result = GenerateInviteLinkResult.builder()
            .sessionId(sessionIdString)
            .inviteLink("/join?sessionId=" + sessionIdString)
            .inviteCode(sessionIdString)
            .build();

        when(authentication.getPrincipal()).thenReturn(userId);
        when(generateInviteLinkUseCase.execute(any(GenerateInviteLinkCommand.class))).thenReturn(result);

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
        verify(generateInviteLinkUseCase).execute(any(GenerateInviteLinkCommand.class));
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
        verify(broadcastSessionUpdateUseCase).execute(sessionIdString);
    }

    @Test
    void shouldGetSessionDetailsSuccessfully() {
        // Given
        Long userId = 1L;
        GetSessionDetailsResult.ParticipantInfo participantInfo = GetSessionDetailsResult.ParticipantInfo.builder()
            .participantId(200L)
            .userId(userId)
            .username("testuser")
            .email("test@example.com")
            .joinedAt("2024-01-01T00:00:00")
            .build();

        GetSessionDetailsResult result = GetSessionDetailsResult.builder()
            .id(sessionId)
            .sessionId(sessionIdString)
            .initiatorId(initiatorId)
            .initiatorUsername("initiator")
            .status(SessionStatus.ACTIVE.getValue())
            .createdAt("2024-01-01T00:00:00")
            .participants(java.util.List.of(participantInfo))
            .participantCount(1L)
            .build();

        when(authentication.getPrincipal()).thenReturn(userId);
        when(getSessionDetailsUseCase.execute(any(GetSessionDetailsCommand.class))).thenReturn(result);

        // When
        ResponseEntity<SessionDetailResponse> response = sessionController.getSessionDetails(
            sessionIdString,
            authentication
        );

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        SessionDetailResponse responseBody = response.getBody();
        assertEquals(sessionId, responseBody.getId());
        assertEquals(sessionIdString, responseBody.getSessionId());
        assertEquals(initiatorId, responseBody.getInitiatorId());
        assertEquals("initiator", responseBody.getInitiatorUsername());
        assertEquals(SessionStatus.ACTIVE.getValue(), responseBody.getStatus());
        assertEquals(1, responseBody.getParticipants().size());
        assertEquals(1L, responseBody.getParticipantCount());

        verify(authentication).getPrincipal();
        verify(getSessionDetailsUseCase).execute(any(GetSessionDetailsCommand.class));
    }

    @Test
    void shouldEndSessionSuccessfully() {
        // Given
        Long userId = 1L;
        when(authentication.getPrincipal()).thenReturn(userId);

        EndSessionResult result = EndSessionResult.builder()
            .sessionId(100L)
            .sessionIdString(sessionIdString)
            .status("Ended")
            .endedAt("2024-01-01T12:00:00")
            .message("Session ended successfully")
            .build();

        when(endSessionUseCase.execute(any(EndSessionCommand.class))).thenReturn(result);

        // When
        ResponseEntity<EndSessionResponse> response = sessionController.endSession(sessionIdString, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getSessionId());
        assertEquals(sessionIdString, response.getBody().getSessionIdString());
        assertEquals("Ended", response.getBody().getStatus());
        assertEquals("Session ended successfully", response.getBody().getMessage());
        assertEquals("2024-01-01T12:00:00", response.getBody().getEndedAt());

        verify(authentication).getPrincipal();
        verify(endSessionUseCase).execute(any(EndSessionCommand.class));
    }
}

