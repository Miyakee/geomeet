package com.geomeet.api.infrastructure.persistence.mapper;

import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import com.geomeet.api.infrastructure.persistence.entity.SessionEntity;

/**
 * Mapper to convert between Domain Session aggregate and JPA SessionEntity.
 */
public class SessionMapper {

    public static Session toDomain(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Session.reconstruct(
            entity.getId(),
            new SessionId(entity.getSessionId()),
            entity.getInitiatorId(),
            SessionStatus.fromString(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    public static SessionEntity toEntity(Session domain) {
        if (domain == null) {
            return null;
        }
        return SessionEntity.builder()
            .id(domain.getId())
            .sessionId(domain.getSessionId().getValue())
            .initiatorId(domain.getInitiatorId())
            .status(domain.getStatus().getValue())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .createdBy(domain.getCreatedBy())
            .updatedBy(domain.getUpdatedBy())
            .build();
    }
}

