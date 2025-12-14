package com.geomeet.api.infrastructure.persistence.mapper;

import com.geomeet.api.domain.entity.ParticipantLocation;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.infrastructure.persistence.entity.ParticipantLocationEntity;
import org.mapstruct.Mapper;

/**
 * Mapper to convert between Domain ParticipantLocation and JPA ParticipantLocationEntity.
 * Uses MapStruct for automatic mapping generation.
 */
@Mapper(componentModel = "spring")
public interface ParticipantLocationMapper {

    default ParticipantLocation toDomain(ParticipantLocationEntity entity) {
        if (entity == null) {
            return null;
        }
        return ParticipantLocation.reconstruct(
            entity.getId(),
            entity.getParticipantId(),
            entity.getSessionId(),
            entity.getUserId(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getAccuracy(),
            entity.getUpdatedAt(),
            entity.getCreatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    default ParticipantLocationEntity toEntity(ParticipantLocation domain) {
        if (domain == null) {
            return null;
        }
        return ParticipantLocationEntity.builder()
            .id(domain.getId())
            .participantId(domain.getParticipantId())
            .sessionId(domain.getSessionId())
            .userId(domain.getUserId())
            .latitude(domain.getLocation().getLatitude().getValue())
            .longitude(domain.getLocation().getLongitude().getValue())
            .accuracy(domain.getLocation().getAccuracy())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .createdBy(domain.getCreatedBy())
            .updatedBy(domain.getUpdatedBy())
            .build();
    }
}

