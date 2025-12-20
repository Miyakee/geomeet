package com.geomeet.api.infrastructure.persistence;

import com.geomeet.api.application.usecase.auth.UserRepository;
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

  public UserRepositoryImpl(JpaUserRepository jpaUserRepository,
                            @SuppressWarnings("unused") UserMapper userMapper) {
    this.jpaUserRepository = jpaUserRepository;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username)
        .map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email)
        .map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
    return jpaUserRepository.findByUsernameOrEmail(usernameOrEmail)
        .map(UserMapper::toDomain);
  }

  @Override
  public User save(User user) {
    UserEntity entity = UserMapper.toEntity(user);
    UserEntity savedEntity = jpaUserRepository.save(entity);
    return UserMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<User> findById(Long id) {
    return jpaUserRepository.findById(id)
        .map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmailAndUserName(String email, String username) {
    return jpaUserRepository.findByEmailOrUsername(email, username).map(UserMapper::toDomain);
  }

}

