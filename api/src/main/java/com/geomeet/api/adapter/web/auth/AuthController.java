package com.geomeet.api.adapter.web.auth;

import com.geomeet.api.adapter.web.auth.dto.LoginRequest;
import com.geomeet.api.adapter.web.auth.dto.LoginResponse;
import com.geomeet.api.adapter.web.auth.dto.RegisterRequest;
import com.geomeet.api.application.command.LoginCommand;
import com.geomeet.api.application.command.RegisterCommand;
import com.geomeet.api.application.result.LoginResult;
import com.geomeet.api.application.usecase.auth.LoginUseCase;
import com.geomeet.api.application.usecase.auth.RegisterUseCase;
import com.geomeet.api.infrastructure.security.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.geomeet.api.adapter.web.util.ResponseUtil.ok;

/**
 * Web adapter (controller) for authentication operations.
 * Handles user login and registration.
 */
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs for user login and registration")
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final JwtTokenService jwtTokenService;
  private final RegisterUseCase registerUseCase;

  @Operation(
      summary = "User login",
      description = "Authenticate user with username/email and password. Returns JWT token for subsequent API calls."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login successful"),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "400", description = "Invalid request data")
  })
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginCommand command = LoginCommand.of(
        request.getUsernameOrEmail(),
        request.getPassword()
    );

    LoginResult result = loginUseCase.execute(command);
    String token = jwtTokenService.generateToken(result.getUserId(), result.getUsername());

    return ok(LoginResponse.toResponse(token, result));
  }

  @Operation(
      summary = "User registration",
      description = "Register a new user account. Returns JWT token for immediate authentication."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Registration successful"),
      @ApiResponse(responseCode = "400", description = "Invalid request data or user already exists"),
      @ApiResponse(responseCode = "422", description = "Validation failed")
  })
  @PostMapping("/register")
  public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
    RegisterCommand command = RegisterCommand.of(request.getUsername(), request.getPassword(),
        request.getEmail(),
        request.getVerificationCode());

    LoginResult result = registerUseCase.execute(command);
    String token = jwtTokenService.generateToken(result.getUserId(), result.getUsername());

    return ok(LoginResponse.toResponse(token, result));
  }
}

