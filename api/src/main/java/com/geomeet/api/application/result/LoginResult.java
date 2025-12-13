package com.geomeet.api.application.result;

/**
 * Result object for login use case.
 * Represents the output of the login operation.
 */
public class LoginResult {

    private final Long userId;
    private final String username;
    private final String email;

    public LoginResult(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}

