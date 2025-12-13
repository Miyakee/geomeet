package com.geomeet.api.infrastructure.persistence.mapper;

import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import com.geomeet.api.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

/**
 * Mapper to convert between Domain User aggregate and JPA UserEntity.
 * Uses MapStruct for automatic mapping generation.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    default User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.reconstruct(
            entity.getId(),
            new Username(entity.getUsername()),
            new Email(entity.getEmail()),
            new PasswordHash(entity.getPasswordHash()),
            entity.getActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    default UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return UserEntity.builder()
            .id(domain.getId())
            .username(domain.getUsername().getValue())
            .email(domain.getEmail().getValue())
            .passwordHash(domain.getPasswordHash().getValue())
            .active(domain.getActive())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .createdBy(domain.getCreatedBy())
            .updatedBy(domain.getUpdatedBy())
            .build();
    }
}
