package com.geomeet.api.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.infrastructure.persistence.entity.SessionParticipantEntity;
import com.geomeet.api.infrastructure.persistence.mapper.SessionParticipantMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionParticipantRepositoryImplTest {

    @Mock
    private JpaSessionParticipantRepository jpaSessionParticipantRepository;

    @Mock
    private SessionParticipantMapper sessionParticipantMapper;

    @InjectMocks
    private SessionParticipantRepositoryImpl sessionParticipantRepository;

    private SessionParticipantEntity participantEntity;
    private SessionParticipant domainParticipant;
    private Long sessionId;
    private Long userId;
    private Long participantId;

    @BeforeEach
    void setUp() {
        sessionId = 100L;
        userId = 1L;
        participantId = 1L;

        participantEntity = SessionParticipantEntity.builder()
            .id(participantId)
            .sessionId(sessionId)
            .userId(userId)
            .joinedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        domainParticipant = SessionParticipant.reconstruct(
            participantId,
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
    void shouldSaveParticipant() {
        // Given
        when(sessionParticipantMapper.toEntity(any(SessionParticipant.class)))
            .thenReturn(participantEntity);
        when(jpaSessionParticipantRepository.save(any(SessionParticipantEntity.class)))
            .thenReturn(participantEntity);
        when(sessionParticipantMapper.toDomain(any(SessionParticipantEntity.class)))
            .thenReturn(domainParticipant);

        // When
        SessionParticipant savedParticipant = sessionParticipantRepository.save(domainParticipant);

        // Then
        assertNotNull(savedParticipant);
        verify(sessionParticipantMapper).toEntity(domainParticipant);
        verify(jpaSessionParticipantRepository).save(participantEntity);
        verify(sessionParticipantMapper).toDomain(participantEntity);
    }

    @Test
    void shouldFindBySessionId() {
        // Given
        List<SessionParticipantEntity> entities = Arrays.asList(participantEntity);
        when(jpaSessionParticipantRepository.findBySessionId(sessionId)).thenReturn(entities);
        when(sessionParticipantMapper.toDomain(participantEntity)).thenReturn(domainParticipant);

        // When
        List<SessionParticipant> result = sessionParticipantRepository.findBySessionId(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jpaSessionParticipantRepository).findBySessionId(sessionId);
        verify(sessionParticipantMapper).toDomain(participantEntity);
    }

    @Test
    void shouldFindBySessionIdAndUserId() {
        // Given
        when(jpaSessionParticipantRepository.findBySessionIdAndUserId(sessionId, userId))
            .thenReturn(Optional.of(participantEntity));
        when(sessionParticipantMapper.toDomain(participantEntity)).thenReturn(domainParticipant);

        // When
        Optional<SessionParticipant> result = sessionParticipantRepository
            .findBySessionIdAndUserId(sessionId, userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sessionId, result.get().getSessionId());
        assertEquals(userId, result.get().getUserId());
        verify(jpaSessionParticipantRepository).findBySessionIdAndUserId(sessionId, userId);
        verify(sessionParticipantMapper).toDomain(participantEntity);
    }

    @Test
    void shouldReturnEmptyWhenSessionIdAndUserIdNotFound() {
        // Given
        when(jpaSessionParticipantRepository.findBySessionIdAndUserId(sessionId, userId))
            .thenReturn(Optional.empty());

        // When
        Optional<SessionParticipant> result = sessionParticipantRepository
            .findBySessionIdAndUserId(sessionId, userId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaSessionParticipantRepository).findBySessionIdAndUserId(sessionId, userId);
    }

    @Test
    void shouldCheckExistsBySessionIdAndUserId() {
        // Given
        when(jpaSessionParticipantRepository.existsBySessionIdAndUserId(sessionId, userId))
            .thenReturn(true);

        // When
        boolean exists = sessionParticipantRepository.existsBySessionIdAndUserId(sessionId, userId);

        // Then
        assertTrue(exists);
        verify(jpaSessionParticipantRepository).existsBySessionIdAndUserId(sessionId, userId);
    }

    @Test
    void shouldCountBySessionId() {
        // Given
        long expectedCount = 5L;
        when(jpaSessionParticipantRepository.countBySessionId(sessionId)).thenReturn(expectedCount);

        // When
        long count = sessionParticipantRepository.countBySessionId(sessionId);

        // Then
        assertEquals(expectedCount, count);
        verify(jpaSessionParticipantRepository).countBySessionId(sessionId);
    }
}

