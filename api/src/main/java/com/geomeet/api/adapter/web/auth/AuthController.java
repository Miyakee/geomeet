package com.geomeet.api.adapter.web.auth;

import com.geomeet.api.adapter.web.auth.dto.ErrorResponse;
import com.geomeet.api.adapter.web.auth.dto.LoginRequest;
import com.geomeet.api.adapter.web.auth.dto.LoginResponse;
import com.geomeet.api.application.command.LoginCommand;
import com.geomeet.api.application.result.LoginResult;
import com.geomeet.api.application.usecase.LoginUseCase;
import com.geomeet.api.domain.exception.DomainException;
import com.geomeet.api.infrastructure.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web adapter (controller) for authentication.
 * This is the entry point from the outside world.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final JwtTokenService jwtTokenService;

    public AuthController(LoginUseCase loginUseCase, JwtTokenService jwtTokenService) {
        this.loginUseCase = loginUseCase;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginCommand command = new LoginCommand(
                request.getUsernameOrEmail(),
                request.getPassword()
            );

            LoginResult result = loginUseCase.execute(command);

            String token = jwtTokenService.generateToken(result.getUserId(), result.getUsername());

            LoginResponse response = new LoginResponse(
                token,
                result.getUsername(),
                result.getEmail(),
                "Login successful"
            );

            return ResponseEntity.ok(response);
        } catch (DomainException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                e.getMessage(),
                "/api/auth/login"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}

