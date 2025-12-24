package com.geomeet.api.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.InviteCode;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import com.geomeet.api.infrastructure.persistence.entity.SessionEntity;
import com.geomeet.api.infrastructure.persistence.mapper.SessionMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionRepositoryImplTest {

    @Mock
    private JpaSessionRepository jpaSessionRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionRepositoryImpl sessionRepository;

    private SessionEntity sessionEntity;
    private Session domainSession;
    private SessionId sessionId;
    private Long sessionDbId;

    @BeforeEach
    void setUp() {
        sessionDbId = 100L;
        sessionId = SessionId.generate();
        InviteCode inviteCode = InviteCode.generate();
        sessionEntity = SessionEntity.builder()
            .id(sessionDbId)
            .sessionId(sessionId.getValue())
            .inviteCode(inviteCode.getValue())
            .initiatorId(1L)
            .status(SessionStatus.ACTIVE.getValue())
            .build();

        domainSession = Session.reconstruct(
            sessionDbId,
            sessionId,
            1L,
            SessionStatus.ACTIVE,
            null,
            null,
            null,
            null
        );
    }

    @Test
    void shouldSaveSession() {
        // Given
        when(sessionMapper.toEntity(any(Session.class))).thenReturn(sessionEntity);
        when(jpaSessionRepository.save(any(SessionEntity.class))).thenReturn(sessionEntity);
        when(sessionMapper.toDomain(any(SessionEntity.class))).thenReturn(domainSession);

        // When
        Session savedSession = sessionRepository.save(domainSession);

        // Then
        assertNotNull(savedSession);
        verify(sessionMapper).toEntity(domainSession);
        verify(jpaSessionRepository).save(sessionEntity);
        verify(sessionMapper).toDomain(sessionEntity);
    }

    @Test
    void shouldFindBySessionId() {
        // Given
        when(jpaSessionRepository.findBySessionId(sessionId.getValue()))
            .thenReturn(Optional.of(sessionEntity));
        when(sessionMapper.toDomain(sessionEntity)).thenReturn(domainSession);

        // When
        Optional<Session> result = sessionRepository.findBySessionId(sessionId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sessionId.getValue(), result.get().getSessionId().getValue());
        verify(jpaSessionRepository).findBySessionId(sessionId.getValue());
        verify(sessionMapper).toDomain(sessionEntity);
    }

    @Test
    void shouldReturnEmptyWhenSessionIdNotFound() {
        // Given
        SessionId nonExistentId = SessionId.generate();
        when(jpaSessionRepository.findBySessionId(nonExistentId.getValue()))
            .thenReturn(Optional.empty());

        // When
        Optional<Session> result = sessionRepository.findBySessionId(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaSessionRepository).findBySessionId(nonExistentId.getValue());
    }

    @Test
    void shouldFindById() {
        // Given
        when(jpaSessionRepository.findById(sessionDbId)).thenReturn(Optional.of(sessionEntity));
        when(sessionMapper.toDomain(sessionEntity)).thenReturn(domainSession);

        // When
        Optional<Session> result = sessionRepository.findById(sessionDbId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sessionDbId, result.get().getId());
        verify(jpaSessionRepository).findById(sessionDbId);
        verify(sessionMapper).toDomain(sessionEntity);
    }

    @Test
    void shouldReturnEmptyWhenIdNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(jpaSessionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Session> result = sessionRepository.findById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaSessionRepository).findById(nonExistentId);
    }
}

