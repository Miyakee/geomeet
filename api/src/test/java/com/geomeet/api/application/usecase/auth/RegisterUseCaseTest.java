package com.geomeet.api.application.usecase.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.RegisterCommand;
import com.geomeet.api.application.result.LoginResult;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.exception.InvalidRegisterException;
import com.geomeet.api.domain.service.PasswordEncoder;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUseCase registerUseCase;

    private User existingUser;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterUseCase(userRepository, passwordEncoder);
        existingUser = User.create(
            new Username("existinguser"),
            new Email("existing@example.com"),
            new PasswordHash("$2a$12$hashedpassword")
        );
    }

    @Test
    void shouldExecuteRegisterSuccessfully() {
        // Given
        RegisterCommand command = RegisterCommand.of(
            "newuser",
            "password123",
            "newuser@example.com",
            "123456"
        );
        when(userRepository.findByEmailAndUserName("newuser@example.com", "newuser"))
            .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Simulate setting an ID by reconstructing the user with an ID
            return User.reconstruct(
                1L,
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getCreatedBy(),
                user.getUpdatedBy()
            );
        });

        // When
        LoginResult result = registerUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        verify(userRepository).findByEmailAndUserName("newuser@example.com", "newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterCommand command = RegisterCommand.of(
            "newuser",
            "password123",
            "existing@example.com",
            "123456"
        );
        when(userRepository.findByEmailAndUserName("existing@example.com", "newuser"))
            .thenReturn(Optional.of(existingUser));

        // When & Then
        InvalidRegisterException exception = assertThrows(
            InvalidRegisterException.class,
            () -> registerUseCase.execute(command)
        );
        assertEquals("Invalid email: existing email", exception.getMessage());
        verify(userRepository).findByEmailAndUserName("existing@example.com", "newuser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        RegisterCommand command = RegisterCommand.of(
            "existinguser",
            "password123",
            "newemail@example.com",
            "123456"
        );
        when(userRepository.findByEmailAndUserName("newemail@example.com", "existinguser"))
            .thenReturn(Optional.of(existingUser));

        // When & Then
        InvalidRegisterException exception = assertThrows(
            InvalidRegisterException.class,
            () -> registerUseCase.execute(command)
        );
        assertEquals("Invalid email: existing email", exception.getMessage());
        verify(userRepository).findByEmailAndUserName("newemail@example.com", "existinguser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}

