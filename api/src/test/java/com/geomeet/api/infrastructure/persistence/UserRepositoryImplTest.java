package com.geomeet.api.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import com.geomeet.api.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    private UserEntity userEntity;
    private User domainUser;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity("testuser", "test@example.com", "$2a$12$hashedpassword");
        userEntity.setId(1L);
        userEntity.setActive(true);

        domainUser = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            new PasswordHash("$2a$12$hashedpassword")
        );
    }

    @Test
    void shouldFindByUsername() {
        when(jpaUserRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        Optional<User> result = userRepository.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername().getValue());
        verify(jpaUserRepository).findByUsername("testuser");
    }

    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        when(jpaUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertFalse(result.isPresent());
        verify(jpaUserRepository).findByUsername("nonexistent");
    }

    @Test
    void shouldFindByEmail() {
        when(jpaUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));

        Optional<User> result = userRepository.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail().getValue());
        verify(jpaUserRepository).findByEmail("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        when(jpaUserRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
        verify(jpaUserRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void shouldFindByUsernameOrEmail() {
        when(jpaUserRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(userEntity));

        Optional<User> result = userRepository.findByUsernameOrEmail("testuser");

        assertTrue(result.isPresent());
        verify(jpaUserRepository).findByUsernameOrEmail("testuser");
    }

    @Test
    void shouldSaveUser() {
        when(jpaUserRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        User savedUser = userRepository.save(domainUser);

        assertNotNull(savedUser);
        verify(jpaUserRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldFindById() {
        when(jpaUserRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        Optional<User> result = userRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(jpaUserRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenIdNotFound() {
        when(jpaUserRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findById(999L);

        assertFalse(result.isPresent());
        verify(jpaUserRepository).findById(999L);
    }
}

