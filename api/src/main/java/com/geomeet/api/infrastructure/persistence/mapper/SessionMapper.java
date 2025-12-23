package com.geomeet.api.infrastructure.persistence.mapper;

import com.geomeet.api.domain.entity.Session;
import com.geomeet.api.domain.valueobject.InviteCode;
import com.geomeet.api.domain.valueobject.Location;
import com.geomeet.api.domain.valueobject.SessionId;
import com.geomeet.api.domain.valueobject.SessionStatus;
import com.geomeet.api.infrastructure.persistence.entity.SessionEntity;
import org.mapstruct.Mapper;

/**
 * Mapper to convert between Domain Session aggregate and JPA SessionEntity.
 * Uses MapStruct for automatic mapping generation.
 */
@Mapper(componentModel = "spring")
public interface SessionMapper {

    default Session toDomain(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        Location meetingLocation = null;
        if (entity.getMeetingLocationLatitude() != null && entity.getMeetingLocationLongitude() != null) {
            meetingLocation = Location.of(
                entity.getMeetingLocationLatitude(),
                entity.getMeetingLocationLongitude()
            );
        }
        return Session.reconstruct(
            entity.getId(),
            SessionId.fromString(entity.getSessionId()),
            InviteCode.fromString(entity.getInviteCode()),
            entity.getInitiatorId(),
            SessionStatus.fromString(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy(),
            meetingLocation
        );
    }

    default SessionEntity toEntity(Session domain) {
        if (domain == null) {
            return null;
        }
        SessionEntity.SessionEntityBuilder builder = SessionEntity.builder()
            .id(domain.getId())
            .sessionId(domain.getSessionId().getValue())
            .inviteCode(domain.getInviteCode() != null ? domain.getInviteCode().getValue() : null)
            .initiatorId(domain.getInitiatorId())
            .status(domain.getStatus().getValue())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .createdBy(domain.getCreatedBy())
            .updatedBy(domain.getUpdatedBy());
        
        if (domain.getMeetingLocation() != null) {
            builder.meetingLocationLatitude(domain.getMeetingLocation().getLatitude().getValue())
                   .meetingLocationLongitude(domain.getMeetingLocation().getLongitude().getValue());
        }
        
        return builder.build();
    }
}
