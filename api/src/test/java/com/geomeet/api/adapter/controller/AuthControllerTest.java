package com.geomeet.api.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geomeet.api.adapter.web.auth.dto.RegisterRequest;
import com.geomeet.api.adapter.web.auth.dto.LoginRequest;
import com.geomeet.api.application.usecase.auth.UserRepository;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.service.PasswordEncoder;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Create a test user using domain factory method
        String passwordHash = passwordEncoder.encode("test123");
        User user = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            new PasswordHash(passwordHash)
        );
        userRepository.save(user);
    }

    @Test
    void testLoginSuccess() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("testuser", "test123")
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("testuser", "wrongpassword")
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Authentication Failed"))
            .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("nonexistent", "password")
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Authentication Failed"))
            .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void testLoginWithEmail() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("test@example.com", "test123")
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testRegisterSuccess() throws Exception {
        String registerRequest = objectMapper.writeValueAsString(
            new RegisterRequest("newuser", "password123", "newuser@example.com", "123456")
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void testRegisterWithExistingEmail() throws Exception {
        String registerRequest = objectMapper.writeValueAsString(
            new RegisterRequest("differentuser", "password123", "test@example.com", "123456")
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("register staff Failed"))
            .andExpect(jsonPath("$.message").value("Invalid email: existing email"));
    }

    @Test
    void testRegisterWithExistingUsername() throws Exception {
        String registerRequest = objectMapper.writeValueAsString(
            new RegisterRequest("testuser", "password123", "different@example.com", "123456")
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("register staff Failed"))
            .andExpect(jsonPath("$.message").value("Invalid email: existing email"));
    }
}

