package com.geomeet.api.application.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for login use case.
 * Represents the output of the login operation.
 */
@Getter
@Builder
public class LoginResult {

    private final Long userId;
    private final String username;
    private final String email;
}

