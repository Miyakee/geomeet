package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.application.usecase.UserRepository;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.infrastructure.persistence.entity.UserEntity;
import com.geomeet.api.infrastructure.persistence.mapper.UserMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Implementation of UserRepository using JPA.
 * This adapter converts between Domain User and JPA UserEntity.
 */
@Component
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    public UserRepositoryImpl(JpaUserRepository jpaUserRepository, UserMapper userMapper) {
        this.jpaUserRepository = jpaUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username)
            .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
            .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return jpaUserRepository.findByUsernameOrEmail(usernameOrEmail)
            .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id)
            .map(userMapper::toDomain);
    }
}

