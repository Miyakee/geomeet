package com.geomeet.api.infrastructure.persistence.mapper;

import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import com.geomeet.api.infrastructure.persistence.entity.UserEntity;

/**
 * Mapper to convert between Domain User aggregate and JPA UserEntity.
 */
public class UserMapper {

    public static User toDomain(UserEntity entity) {
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

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity(
            domain.getUsername().getValue(),
            domain.getEmail().getValue(),
            domain.getPasswordHash().getValue()
        );
        entity.setId(domain.getId());
        entity.setActive(domain.getActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setUpdatedBy(domain.getUpdatedBy());
        return entity;
    }
}

