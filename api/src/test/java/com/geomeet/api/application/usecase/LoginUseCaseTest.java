package com.geomeet.api.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geomeet.api.application.command.LoginCommand;
import com.geomeet.api.application.result.LoginResult;
import com.geomeet.api.application.usecase.login.LoginUseCase;
import com.geomeet.api.application.usecase.login.UserRepository;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.exception.InactiveUserException;
import com.geomeet.api.domain.exception.InvalidCredentialsException;
import com.geomeet.api.domain.service.PasswordEncoder;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private LoginUseCase loginUseCase;

    private User testUser;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(userRepository, passwordEncoder);
        testUser = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            new PasswordHash("$2a$12$hashedpassword")
        );
    }

    @Test
    void shouldExecuteLoginSuccessfully() {
        // Given
        LoginCommand command = new LoginCommand("testuser", "password123");
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$12$hashedpassword")).thenReturn(true);

        // When
        LoginResult result = loginUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", "$2a$12$hashedpassword");
    }

    @Test
    void shouldExecuteLoginWithEmail() {
        // Given
        LoginCommand command = new LoginCommand("test@example.com", "password123");
        when(userRepository.findByUsernameOrEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$12$hashedpassword")).thenReturn(true);

        // When
        LoginResult result = loginUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsernameOrEmail("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginCommand command = new LoginCommand("nonexistent", "password123");
        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(command));
        verify(userRepository).findByUsernameOrEmail("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        LoginCommand command = new LoginCommand("testuser", "wrongpassword");
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$12$hashedpassword")).thenReturn(false);

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(command));
        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("wrongpassword", "$2a$12$hashedpassword");
    }

    @Test
    void shouldThrowExceptionWhenUserIsInactive() {
        // Given
        User inactiveUser = User.create(
            new Username("inactive"),
            new Email("inactive@example.com"),
            new PasswordHash("$2a$12$hashedpassword")
        );
        inactiveUser.deactivate();

        LoginCommand command = new LoginCommand("inactive", "password123");
        when(userRepository.findByUsernameOrEmail("inactive")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThrows(InactiveUserException.class, () -> loginUseCase.execute(command));
        verify(userRepository).findByUsernameOrEmail("inactive");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}

