package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.GetSessionDetailsCommand;
import com.geomeet.api.application.result.GetSessionDetailsResult;
import com.geomeet.api.application.usecase.auth.UserRepository;
import com.geomeet.api.application.usecase.session.GetSessionDetailsUseCase;
import com.geomeet.api.application.usecase.session.SessionParticipantRepository;
import com.geomeet.api.application.usecase.session.SessionRepository;
import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import com.geomeet.api.domain.valueobject.Username;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSessionDetailsUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    @Mock
    private UserRepository userRepository;

    private GetSessionDetailsUseCase getSessionDetailsUseCase;

    private Long sessionId;
    private String sessionIdString;
    private Long initiatorId;
    private Long userId;
    private Session session;
    private User initiator;
    private User participantUser;
    private SessionParticipant participant;

    @BeforeEach
    void setUp() {
        getSessionDetailsUseCase = new GetSessionDetailsUseCase(
            sessionRepository,
            sessionParticipantRepository,
            userRepository
        );

        sessionId = 100L;
        sessionIdString = "test-session-id-123";
        initiatorId = 1L;
        userId = 2L;

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

        initiator = User.reconstruct(
            initiatorId,
            new Username("initiator"),
            new Email("initiator@example.com"),
            new PasswordHash("hash"),
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        participantUser = User.reconstruct(
            userId,
            new Username("participant"),
            new Email("participant@example.com"),
            new PasswordHash("hash"),
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        participant = SessionParticipant.reconstruct(
            200L,
            sessionId,
            userId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
    }

    @Test
    void shouldExecuteGetSessionDetailsSuccessfullyAsInitiator() {
        // Given
        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, initiatorId))
            .thenReturn(false);
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId)).thenReturn(List.of(participant));
        when(userRepository.findById(userId)).thenReturn(Optional.of(participantUser));

        // When
        GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        assertEquals(sessionIdString, result.getSessionId());
        assertEquals(initiatorId, result.getInitiatorId());
        assertEquals("initiator", result.getInitiatorUsername());
        assertEquals(SessionStatus.ACTIVE.getValue(), result.getStatus());
        // Initiator is automatically added to participants list, so we have initiator + 1 participant = 2
        assertEquals(2, result.getParticipants().size());
        assertEquals(2L, result.getParticipantCount());
        // First participant should be the initiator
        assertEquals("initiator", result.getParticipants().get(0).getUsername());
        assertEquals(initiatorId, result.getParticipants().get(0).getUserId());

        verify(sessionRepository).findBySessionId(any(SessionId.class));
        verify(sessionParticipantRepository).existsBySessionIdAndUserId(sessionId, initiatorId);
        verify(userRepository).findById(initiatorId);
    }

    @Test
    void shouldExecuteGetSessionDetailsSuccessfullyAsParticipant() {
        // Given
        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, userId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, userId))
            .thenReturn(true);
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId)).thenReturn(List.of(participant));
        when(userRepository.findById(userId)).thenReturn(Optional.of(participantUser));

        // When
        GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        // Initiator is automatically added to participants list, so we have initiator + 1 participant = 2
        assertEquals(2, result.getParticipants().size());
        assertEquals(2L, result.getParticipantCount());
        // First participant should be the initiator
        assertEquals("initiator", result.getParticipants().get(0).getUsername());
        assertEquals(initiatorId, result.getParticipants().get(0).getUserId());
        // Second participant should be the joined participant
        assertEquals("participant", result.getParticipants().get(1).getUsername());
        assertEquals(userId, result.getParticipants().get(1).getUserId());

        verify(sessionRepository).findBySessionId(any(SessionId.class));
        verify(sessionParticipantRepository).existsBySessionIdAndUserId(sessionId, userId);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, userId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            getSessionDetailsUseCase.execute(command);
        });

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).findBySessionId(any(SessionId.class));
    }

    @Test
    void shouldThrowExceptionWhenAccessDenied() {
        // Given
        Long unauthorizedUserId = 999L;
        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, unauthorizedUserId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, unauthorizedUserId))
            .thenReturn(false);

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            getSessionDetailsUseCase.execute(command);
        });

        assertEquals("Access denied: User is not a participant or initiator", exception.getMessage());
        verify(sessionRepository).findBySessionId(any(SessionId.class));
        verify(sessionParticipantRepository).existsBySessionIdAndUserId(sessionId, unauthorizedUserId);
    }

    @Test
    void shouldThrowExceptionWhenInitiatorNotFound() {
        // Given
        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, initiatorId))
            .thenReturn(false);
        when(userRepository.findById(initiatorId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            getSessionDetailsUseCase.execute(command);
        });

        assertEquals("Initiator not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenParticipantUserNotFound() {
        // Given
        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, initiatorId))
            .thenReturn(false);
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId)).thenReturn(List.of(participant));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> {
            getSessionDetailsUseCase.execute(command);
        });

        assertEquals("User not found for participant", exception.getMessage());
    }

    @Test
    void shouldNotAddInitiatorWhenAlreadyInParticipants() {
        // Given - initiator is already a participant
        SessionParticipant initiatorParticipant = SessionParticipant.reconstruct(
            201L,
            sessionId,
            initiatorId,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, initiatorId))
            .thenReturn(true);
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId))
            .thenReturn(List.of(initiatorParticipant, participant));
        when(userRepository.findById(userId)).thenReturn(Optional.of(participantUser));

        // When
        GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getParticipants().size());
        assertEquals(2L, result.getParticipantCount());
        // Initiator should not be duplicated
        long initiatorCount = result.getParticipants().stream()
            .filter(p -> p.getUserId().equals(initiatorId))
            .count();
        assertEquals(1, initiatorCount);
    }

    @Test
    void shouldIncludeMeetingLocationWhenSessionHasMeetingLocation() {
        // Given - session with meeting location
        com.geomeet.api.domain.valueobject.Location meetingLocation = 
            com.geomeet.api.domain.valueobject.Location.of(1.3521, 103.8198);
        Session sessionWithLocation = Session.reconstruct(
            sessionId,
            SessionId.fromString(sessionIdString),
            initiatorId,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null,
            meetingLocation
        );

        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(sessionWithLocation));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, initiatorId))
            .thenReturn(false);
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId)).thenReturn(List.of());

        // When
        GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(1.3521, result.getMeetingLocationLatitude());
        assertEquals(103.8198, result.getMeetingLocationLongitude());
    }

    @Test
    void shouldReturnNullMeetingLocationWhenSessionHasNoMeetingLocation() {
        // Given - session without meeting location (already set up in setUp)
        GetSessionDetailsCommand command = GetSessionDetailsCommand.of(sessionIdString, initiatorId);
        when(sessionRepository.findBySessionId(any(SessionId.class))).thenReturn(Optional.of(session));
        when(sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, initiatorId))
            .thenReturn(false);
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(initiator));
        when(sessionParticipantRepository.findBySessionId(sessionId)).thenReturn(List.of());

        // When
        GetSessionDetailsResult result = getSessionDetailsUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertNull(result.getMeetingLocationLatitude());
        assertNull(result.getMeetingLocationLongitude());
    }
}

