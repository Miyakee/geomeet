package com.geomeet.api.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import com.geomeet.api.infrastructure.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    private UserEntity userEntity;
    private User domainUser;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity("testuser", "test@example.com", "$2a$12$hashedpassword");
        userEntity.setId(1L);
        userEntity.setActive(true);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userEntity.setCreatedBy("admin");
        userEntity.setUpdatedBy("admin");

        domainUser = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            new PasswordHash("$2a$12$hashedpassword")
        );
    }

    @Test
    void shouldMapEntityToDomain() {
        User mappedUser = UserMapper.toDomain(userEntity);

        assertNotNull(mappedUser);
        assertEquals(userEntity.getId(), mappedUser.getId());
        assertEquals(userEntity.getUsername(), mappedUser.getUsername().getValue());
        assertEquals(userEntity.getEmail(), mappedUser.getEmail().getValue());
        assertEquals(userEntity.getPasswordHash(), mappedUser.getPasswordHash().getValue());
        assertEquals(userEntity.getActive(), mappedUser.getActive());
        assertEquals(userEntity.getCreatedAt(), mappedUser.getCreatedAt());
        assertEquals(userEntity.getUpdatedAt(), mappedUser.getUpdatedAt());
        assertEquals(userEntity.getCreatedBy(), mappedUser.getCreatedBy());
        assertEquals(userEntity.getUpdatedBy(), mappedUser.getUpdatedBy());
    }

    @Test
    void shouldMapDomainToEntity() {
        UserEntity mappedEntity = UserMapper.toEntity(domainUser);

        assertNotNull(mappedEntity);
        assertEquals(domainUser.getId(), mappedEntity.getId());
        assertEquals(domainUser.getUsername().getValue(), mappedEntity.getUsername());
        assertEquals(domainUser.getEmail().getValue(), mappedEntity.getEmail());
        assertEquals(domainUser.getPasswordHash().getValue(), mappedEntity.getPasswordHash());
        assertEquals(domainUser.getActive(), mappedEntity.getActive());
        assertEquals(domainUser.getCreatedAt(), mappedEntity.getCreatedAt());
        assertEquals(domainUser.getUpdatedAt(), mappedEntity.getUpdatedAt());
        assertEquals(domainUser.getCreatedBy(), mappedEntity.getCreatedBy());
        assertEquals(domainUser.getUpdatedBy(), mappedEntity.getUpdatedBy());
    }

    @Test
    void shouldReturnNullWhenMappingNullEntity() {
        User mappedUser = UserMapper.toDomain(null);
        assertNull(mappedUser);
    }

    @Test
    void shouldReturnNullWhenMappingNullDomain() {
        UserEntity mappedEntity = UserMapper.toEntity(null);
        assertNull(mappedEntity);
    }

    @Test
    void shouldMapEntityWithNullAuditFields() {
        userEntity.setCreatedBy(null);
        userEntity.setUpdatedBy(null);

        User mappedUser = UserMapper.toDomain(userEntity);

        assertNotNull(mappedUser);
        assertNull(mappedUser.getCreatedBy());
        assertNull(mappedUser.getUpdatedBy());
    }

}

