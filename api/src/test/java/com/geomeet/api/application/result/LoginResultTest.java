package com.geomeet.api.application.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class LoginResultTest {

    @Test
    void shouldCreateLoginResult() {
        LoginResult result = new LoginResult(1L, "testuser", "test@example.com");
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }
}

