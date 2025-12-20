package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for login use case.
 * Represents the input for the login operation.
 */
@Getter
@Builder
public class RegisterCommand {

  private final String username;
  private final String password;
  private final String email;
  private final String verificationCode;

  public static RegisterCommand of(String username,
                                   String password,
                                   String email,
                                   String verificationCode) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email cannot be null or empty");
    }

    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    if (verificationCode == null || verificationCode.isBlank()) {
      throw new IllegalArgumentException("verificationCode cannot be null or empty");
    }

    return RegisterCommand.builder()
        .verificationCode(verificationCode)
        .username(username)
        .password(password)
        .email(email)
        .build();
  }
}

