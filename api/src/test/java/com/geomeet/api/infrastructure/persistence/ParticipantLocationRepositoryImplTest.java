package com.geomeet.api.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.infrastructure.persistence.entity.ParticipantLocationEntity;
import com.geomeet.api.infrastructure.persistence.mapper.ParticipantLocationMapper;
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
class ParticipantLocationRepositoryImplTest {

    @Mock
    private JpaParticipantLocationRepository jpaParticipantLocationRepository;

    @Mock
    private ParticipantLocationMapper participantLocationMapper;

    @InjectMocks
    private ParticipantLocationRepositoryImpl participantLocationRepository;

    private ParticipantLocation domainLocation;
    private ParticipantLocationEntity entityLocation;
    private Long participantId;
    private Long sessionId;
    private Long userId;

    @BeforeEach
    void setUp() {
        participantId = 100L;
        sessionId = 200L;
        userId = 300L;

        domainLocation = ParticipantLocation.reconstruct(
            1L,
            participantId,
            sessionId,
            userId,
            1.3521,
            103.8198,
            10.0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "test-user",
            "test-user"
        );

        entityLocation = ParticipantLocationEntity.builder()
            .id(1L)
            .participantId(participantId)
            .sessionId(sessionId)
            .userId(userId)
            .latitude(1.3521)
            .longitude(103.8198)
            .accuracy(10.0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
    }

    @Test
    void shouldSaveParticipantLocation() {
        // Given
        when(participantLocationMapper.toEntity(domainLocation)).thenReturn(entityLocation);
        when(jpaParticipantLocationRepository.save(entityLocation)).thenReturn(entityLocation);
        when(participantLocationMapper.toDomain(entityLocation)).thenReturn(domainLocation);

        // When
        ParticipantLocation saved = participantLocationRepository.save(domainLocation);

        // Then
        assertNotNull(saved);
        assertEquals(domainLocation.getId(), saved.getId());
        verify(participantLocationMapper).toEntity(domainLocation);
        verify(jpaParticipantLocationRepository).save(entityLocation);
        verify(participantLocationMapper).toDomain(entityLocation);
    }

    @Test
    void shouldFindByParticipantId() {
        // Given
        when(jpaParticipantLocationRepository.findByParticipantId(participantId))
            .thenReturn(Optional.of(entityLocation));
        when(participantLocationMapper.toDomain(entityLocation)).thenReturn(domainLocation);

        // When
        Optional<ParticipantLocation> result = participantLocationRepository.findByParticipantId(participantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(domainLocation.getId(), result.get().getId());
        verify(jpaParticipantLocationRepository).findByParticipantId(participantId);
        verify(participantLocationMapper).toDomain(entityLocation);
    }

    @Test
    void shouldReturnEmptyWhenParticipantIdNotFound() {
        // Given
        when(jpaParticipantLocationRepository.findByParticipantId(participantId))
            .thenReturn(Optional.empty());

        // When
        Optional<ParticipantLocation> result = participantLocationRepository.findByParticipantId(participantId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaParticipantLocationRepository).findByParticipantId(participantId);
    }

    @Test
    void shouldFindBySessionIdAndUserId() {
        // Given
        when(jpaParticipantLocationRepository.findBySessionIdAndUserId(sessionId, userId))
            .thenReturn(Optional.of(entityLocation));
        when(participantLocationMapper.toDomain(entityLocation)).thenReturn(domainLocation);

        // When
        Optional<ParticipantLocation> result = participantLocationRepository.findBySessionIdAndUserId(sessionId, userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(domainLocation.getId(), result.get().getId());
        verify(jpaParticipantLocationRepository).findBySessionIdAndUserId(sessionId, userId);
        verify(participantLocationMapper).toDomain(entityLocation);
    }

    @Test
    void shouldReturnEmptyWhenSessionIdAndUserIdNotFound() {
        // Given
        when(jpaParticipantLocationRepository.findBySessionIdAndUserId(sessionId, userId))
            .thenReturn(Optional.empty());

        // When
        Optional<ParticipantLocation> result = participantLocationRepository.findBySessionIdAndUserId(sessionId, userId);

        // Then
        assertFalse(result.isPresent());
        verify(jpaParticipantLocationRepository).findBySessionIdAndUserId(sessionId, userId);
    }

    @Test
    void shouldFindBySessionId() {
        // Given
        ParticipantLocationEntity entity2 = ParticipantLocationEntity.builder()
            .id(2L)
            .participantId(101L)
            .sessionId(sessionId)
            .userId(301L)
            .latitude(1.2903)
            .longitude(103.8520)
            .accuracy(15.0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        ParticipantLocation domain2 = ParticipantLocation.reconstruct(
            2L,
            101L,
            sessionId,
            301L,
            1.2903,
            103.8520,
            15.0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "test-user",
            "test-user"
        );

        List<ParticipantLocationEntity> entities = Arrays.asList(entityLocation, entity2);
        when(jpaParticipantLocationRepository.findBySessionId(sessionId)).thenReturn(entities);
        when(participantLocationMapper.toDomain(entityLocation)).thenReturn(domainLocation);
        when(participantLocationMapper.toDomain(entity2)).thenReturn(domain2);

        // When
        List<ParticipantLocation> result = participantLocationRepository.findBySessionId(sessionId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(domainLocation.getId(), result.get(0).getId());
        assertEquals(domain2.getId(), result.get(1).getId());
        verify(jpaParticipantLocationRepository).findBySessionId(sessionId);
        verify(participantLocationMapper).toDomain(entityLocation);
        verify(participantLocationMapper).toDomain(entity2);
    }

    @Test
    void shouldReturnEmptyListWhenSessionIdNotFound() {
        // Given
        when(jpaParticipantLocationRepository.findBySessionId(sessionId))
            .thenReturn(Arrays.asList());

        // When
        List<ParticipantLocation> result = participantLocationRepository.findBySessionId(sessionId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaParticipantLocationRepository).findBySessionId(sessionId);
    }
}

