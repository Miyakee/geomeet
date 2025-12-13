package com.geomeet.api.application.usecase;

import com.geomeet.api.application.command.LoginCommand;
import com.geomeet.api.application.result.LoginResult;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.domain.exception.InactiveUserException;
import com.geomeet.api.domain.exception.InvalidCredentialsException;
import com.geomeet.api.domain.service.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for user login.
 * Orchestrates the authentication flow and coordinates domain logic.
 */
@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executes the login use case.
     * Authenticates a user with username/email and password.
     *
     * @param command the login command containing credentials
     * @return login result with user information
     * @throws DomainException if authentication fails
     */
    public LoginResult execute(LoginCommand command) {
        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(command.getUsernameOrEmail())
            .orElseThrow(() -> new InvalidCredentialsException());

        // Check if user account is active
        if (!user.canAuthenticate()) {
            throw new InactiveUserException();
        }

        // Verify password
        if (!passwordEncoder.matches(command.getPassword(), user.getPasswordHash().getValue())) {
            throw new InvalidCredentialsException();
        }

        // Return login result
        return new LoginResult(
            user.getId(),
            user.getUsername().getValue(),
            user.getEmail().getValue()
        );
    }
}


