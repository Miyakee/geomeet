package com.geomeet.api.infrastructure.persistence.mapper;

import com.geomeet.api.domain.entity.SessionParticipant;
import com.geomeet.api.infrastructure.persistence.entity.SessionParticipantEntity;
import org.mapstruct.Mapper;

/**
 * Mapper to convert between Domain SessionParticipant and JPA SessionParticipantEntity.
 * Uses MapStruct for automatic mapping generation.
 */
@Mapper(componentModel = "spring")
public interface SessionParticipantMapper {

    default SessionParticipant toDomain(SessionParticipantEntity entity) {
        if (entity == null) {
            return null;
        }
        return SessionParticipant.reconstruct(
            entity.getId(),
            entity.getSessionId(),
            entity.getUserId(),
            entity.getJoinedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    default SessionParticipantEntity toEntity(SessionParticipant domain) {
        if (domain == null) {
            return null;
        }
        return SessionParticipantEntity.builder()
            .id(domain.getId())
            .sessionId(domain.getSessionId())
            .userId(domain.getUserId())
            .joinedAt(domain.getJoinedAt())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .createdBy(domain.getCreatedBy())
            .updatedBy(domain.getUpdatedBy())
            .build();
    }
}

