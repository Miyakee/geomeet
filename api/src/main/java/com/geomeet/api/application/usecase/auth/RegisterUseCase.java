package com.geomeet.api.application.usecase.auth;

import com.geomeet.api.application.command.RegisterCommand;
import com.geomeet.api.application.result.LoginResult;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.exception.ErrorCode;
import com.geomeet.api.domain.exception.GeomeetDomainException;
import com.geomeet.api.domain.service.PasswordEncoder;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service (Use Case) for user register.
 */
@Service
@AllArgsConstructor
public class RegisterUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;


  /**
   * Executes the register use case.
   * Authenticates a user with username, email, verification code and password.
   *
   * @param command the login command containing credentials
   * @return login result with user information
   * @throws GeomeetDomainException if authentication fails
   */
  public LoginResult execute(RegisterCommand command) {
//     Find user by email
    Optional<User> emailAndUserName =
        userRepository.findByEmailAndUserName(command.getEmail(), command.getUsername());
    if (emailAndUserName.isPresent()) {
      throw ErrorCode.INVALID_EMAIL_OR_USERNAME.toException();
    }

    // create new User
    String adminPasswordHash = passwordEncoder.encode(command.getPassword());
    User newUser = User.create(
        new Username(command.getUsername()),
        new Email(command.getEmail()),
        new PasswordHash(adminPasswordHash)
    );
    User cratedUser = userRepository.save(newUser);

    // Return login result
    return LoginResult.builder()
        .userId(cratedUser.getId())
        .username(cratedUser.getUsername().getValue())
        .email(cratedUser.getEmail().getValue())
        .build();
  }
}


