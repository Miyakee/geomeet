package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.GenerateInviteLinkCommand;
import com.geomeet.api.application.result.GenerateInviteLinkResult;
import com.geomeet.api.application.usecase.session.GenerateInviteLinkUseCase;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateInviteLinkUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    private GenerateInviteLinkUseCase generateInviteLinkUseCase;

    private Long sessionId;
    private String sessionIdString;
    private Long initiatorId;
    private Long otherUserId;
    private Session session;

    @BeforeEach
    void setUp() {
        generateInviteLinkUseCase = new GenerateInviteLinkUseCase(sessionRepository);

        sessionId = 100L;
        sessionIdString = "test-session-id-123";
        initiatorId = 1L;
        otherUserId = 2L;

        SessionId sessionIdVO = SessionId.fromString(sessionIdString);
        session = Session.reconstruct(
            sessionId,
            sessionIdVO,
            initiatorId,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
    }

    @Test
    void shouldExecuteGenerateInviteLinkSuccessfully() {
        // Given
        GenerateInviteLinkCommand command = GenerateInviteLinkCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));

        // When
        GenerateInviteLinkResult result = generateInviteLinkUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(sessionIdString, result.getSessionId());
        assertEquals("/join?sessionId=" + sessionIdString + "&inviteCode=" + session.getInviteCode().getValue(), result.getInviteLink());
        assertEquals(session.getInviteCode().getValue(), result.getInviteCode());

        verify(sessionRepository).findBySessionId(any(SessionId.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        GenerateInviteLinkCommand command = GenerateInviteLinkCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.empty());

        // When & Then
        GeomeetDomainException exception = assertThrows(GeomeetDomainException.class, () -> {
            generateInviteLinkUseCase.execute(command);
        });

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).findBySessionId(any(SessionId.class));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotInitiator() {
        // Given
        GenerateInviteLinkCommand command = GenerateInviteLinkCommand.of(sessionIdString, otherUserId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));

        // When & Then
        GeomeetDomainException exception = assertThrows(GeomeetDomainException.class, () -> {
            generateInviteLinkUseCase.execute(command);
        });

        assertEquals("Only the session initiator can generate invite links", exception.getMessage());
        verify(sessionRepository).findBySessionId(any(SessionId.class));
    }
}

