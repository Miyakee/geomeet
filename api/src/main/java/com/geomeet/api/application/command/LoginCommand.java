package com.geomeet.api.application.command;

import lombok.Builder;
import lombok.Getter;

/**
 * Command object for login use case.
 * Represents the input for the login operation.
 */
@Getter
@Builder
public class LoginCommand {

    private final String usernameOrEmail;
    private final String password;

    public LoginCommand(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username or email cannot be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public static LoginCommand of(String usernameOrEmail, String password) {
        return LoginCommand.builder()
            .usernameOrEmail(usernameOrEmail)
            .password(password)
            .build();
    }
}

