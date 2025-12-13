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

    private CreateSessionUseCase createSessionUseCase;

    private Long initiatorId;

    @BeforeEach
    void setUp() {
        createSessionUseCase = new CreateSessionUseCase(sessionRepository);
        initiatorId = 1L;
    }

    @Test
    void shouldExecuteCreateSessionSuccessfully() {
        // Given
        CreateSessionCommand command = new CreateSessionCommand(initiatorId);
        Session createdSession = Session.create(initiatorId);
        when(sessionRepository.save(any(Session.class))).thenReturn(createdSession);

        // When
        CreateSessionResult result = createSessionUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSessionIdString());
        assertEquals(initiatorId, result.getInitiatorId());
        assertEquals(SessionStatus.ACTIVE.getValue(), result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void shouldGenerateUniqueSessionId() {
        // Given
        CreateSessionCommand command = new CreateSessionCommand(initiatorId);
        Session session1 = Session.create(initiatorId);
        Session session2 = Session.create(initiatorId);
        when(sessionRepository.save(any(Session.class)))
            .thenReturn(session1)
            .thenReturn(session2);

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
        when(sessionRepository.save(any(Session.class))).thenReturn(createdSession);

        // When
        CreateSessionResult result = createSessionUseCase.execute(command);

        // Then
        assertEquals(SessionStatus.ACTIVE.getValue(), result.getStatus());
    }
}

