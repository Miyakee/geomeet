package com.geomeet.api.application.command;

/**
 * Command object for login use case.
 * Represents the input for the login operation.
 */
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

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }
}

