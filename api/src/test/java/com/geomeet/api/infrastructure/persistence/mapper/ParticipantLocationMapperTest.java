package com.geomeet.api.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.infrastructure.persistence.entity.ParticipantLocationEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ParticipantLocationMapperTest {

    private ParticipantLocationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ParticipantLocationMapper.class);
    }

    @Test
    void shouldConvertEntityToDomain() {
        // Given
        ParticipantLocationEntity entity = ParticipantLocationEntity.builder()
            .id(1L)
            .participantId(100L)
            .sessionId(200L)
            .userId(300L)
            .latitude(1.3521)
            .longitude(103.8198)
            .accuracy(10.0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        // When
        ParticipantLocation domain = mapper.toDomain(entity);

        // Then
        assertNotNull(domain);
        assertEquals(entity.getId(), domain.getId());
        assertEquals(entity.getParticipantId(), domain.getParticipantId());
        assertEquals(entity.getSessionId(), domain.getSessionId());
        assertEquals(entity.getUserId(), domain.getUserId());
        assertEquals(entity.getLatitude(), domain.getLocation().getLatitude().getValue());
        assertEquals(entity.getLongitude(), domain.getLocation().getLongitude().getValue());
        assertEquals(entity.getAccuracy(), domain.getLocation().getAccuracy());
        assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), domain.getUpdatedAt());
        assertEquals(entity.getCreatedBy(), domain.getCreatedBy());
        assertEquals(entity.getUpdatedBy(), domain.getUpdatedBy());
    }

    @Test
    void shouldConvertDomainToEntity() {
        // Given
        Location location = Location.of(1.3521, 103.8198, 10.0);
        ParticipantLocation domain = ParticipantLocation.reconstruct(
            1L,
            100L,
            200L,
            300L,
            1.3521,
            103.8198,
            10.0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "test-user",
            "test-user"
        );

        // When
        ParticipantLocationEntity entity = mapper.toEntity(domain);

        // Then
        assertNotNull(entity);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getParticipantId(), entity.getParticipantId());
        assertEquals(domain.getSessionId(), entity.getSessionId());
        assertEquals(domain.getUserId(), entity.getUserId());
        assertEquals(domain.getLocation().getLatitude().getValue(), entity.getLatitude());
        assertEquals(domain.getLocation().getLongitude().getValue(), entity.getLongitude());
        assertEquals(domain.getLocation().getAccuracy(), entity.getAccuracy());
        assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
        assertEquals(domain.getUpdatedAt(), entity.getUpdatedAt());
        assertEquals(domain.getCreatedBy(), entity.getCreatedBy());
        assertEquals(domain.getUpdatedBy(), entity.getUpdatedBy());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        // When
        ParticipantLocation domain = mapper.toDomain(null);

        // Then
        assertNull(domain);
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        // When
        ParticipantLocationEntity entity = mapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    void shouldConvertEntityToDomainWithoutAccuracy() {
        // Given
        ParticipantLocationEntity entity = ParticipantLocationEntity.builder()
            .id(1L)
            .participantId(100L)
            .sessionId(200L)
            .userId(300L)
            .latitude(1.3521)
            .longitude(103.8198)
            .accuracy(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy(null)
            .updatedBy(null)
            .build();

        // When
        ParticipantLocation domain = mapper.toDomain(entity);

        // Then
        assertNotNull(domain);
        assertEquals(entity.getAccuracy(), domain.getLocation().getAccuracy());
        assertNull(domain.getLocation().getAccuracy());
    }

    @Test
    void shouldConvertDomainToEntityWithoutAccuracy() {
        // Given
        ParticipantLocation domain = ParticipantLocation.reconstruct(
            1L,
            100L,
            200L,
            300L,
            1.3521,
            103.8198,
            null,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );

        // When
        ParticipantLocationEntity entity = mapper.toEntity(domain);

        // Then
        assertNotNull(entity);
        assertEquals(domain.getLocation().getAccuracy(), entity.getAccuracy());
        assertNull(entity.getAccuracy());
    }
}

