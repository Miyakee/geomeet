package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.CreateSessionCommand;
import com.geomeet.api.application.result.CreateSessionResult;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.valueobject.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSessionUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    private CreateSessionUseCase createSessionUseCase;

    private Long initiatorId;

    @BeforeEach
    void setUp() {
        createSessionUseCase = new CreateSessionUseCase(sessionRepository, sessionParticipantRepository);
        initiatorId = 1L;
    }

    @Test
    void shouldExecuteCreateSessionSuccessfully() {
        // Given
        CreateSessionCommand command = new CreateSessionCommand(initiatorId);
        Session createdSession = Session.create(initiatorId);
        // Set ID for the saved session - use the created session's values
        Session savedSession = Session.reconstruct(
            100L, // sessionId
            createdSession.getSessionId(),
            createdSession.getInitiatorId(),
            createdSession.getStatus(),
            java.time.LocalDateTime.now(), // createdAt
            java.time.LocalDateTime.now(), // updatedAt
            null, // createdBy
            null  // updatedBy
        );
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);
        
        java.time.LocalDateTime participantNow = java.time.LocalDateTime.now();
        SessionParticipant savedParticipant = SessionParticipant.reconstruct(
            200L, // participantId
            savedSession.getId(),
            initiatorId,
            participantNow, // joinedAt
            participantNow, // createdAt
            participantNow, // updatedAt
            null, // createdBy
            null  // updatedBy
        );
        when(sessionParticipantRepository.save(any(SessionParticipant.class))).thenReturn(savedParticipant);

        // When
        CreateSessionResult result = createSessionUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSessionIdString());
        assertEquals(initiatorId, result.getInitiatorId());
        assertEquals(SessionStatus.ACTIVE.getValue(), result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(sessionRepository).save(any(Session.class));
        verify(sessionParticipantRepository).save(any(SessionParticipant.class));
    }

    @Test
    void shouldGenerateUniqueSessionId() {
        // Given
        CreateSessionCommand command = new CreateSessionCommand(initiatorId);
        Session session1 = Session.create(initiatorId);
        Session session2 = Session.create(initiatorId);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Session savedSession1 = Session.reconstruct(
                100L, session1.getSessionId(), initiatorId, session1.getStatus(), now, now, null, null);
        Session savedSession2 = Session.reconstruct(
                101L, session2.getSessionId(), initiatorId, session2.getStatus(), now, now, null, null);
        when(sessionRepository.save(any(Session.class)))
            .thenReturn(savedSession1)
            .thenReturn(savedSession2);
        java.time.LocalDateTime participantNow = java.time.LocalDateTime.now();
        when(sessionParticipantRepository.save(any(SessionParticipant.class)))
            .thenReturn(SessionParticipant.reconstruct(
                    200L, 100L, initiatorId, participantNow, participantNow, participantNow, null, null))
            .thenReturn(SessionParticipant.reconstruct(
                    201L, 101L, initiatorId, participantNow, participantNow, participantNow, null, null));

        // When
        CreateSessionResult result1 = createSessionUseCase.execute(command);
        CreateSessionResult result2 = createSessionUseCase.execute(command);

        // Then
        assertNotEquals(result1.getSessionIdString(), result2.getSessionIdString());
    }

    @Test
    void shouldSetStatusToActive() {
        // Given
        CreateSessionCommand command = new CreateSessionCommand(initiatorId);
        Session createdSession = Session.create(initiatorId);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Session savedSession = Session.reconstruct(
                100L, createdSession.getSessionId(), initiatorId, createdSession.getStatus(), now, now, null, null);
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);
        java.time.LocalDateTime participantNow = java.time.LocalDateTime.now();
        when(sessionParticipantRepository.save(any(SessionParticipant.class)))
            .thenReturn(SessionParticipant.reconstruct(
                    200L, 100L, initiatorId, participantNow, participantNow, participantNow, null, null));

        // When
        CreateSessionResult result = createSessionUseCase.execute(command);

        // Then
        assertEquals(SessionStatus.ACTIVE.getValue(), result.getStatus());
    }
}

