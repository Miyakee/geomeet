package com.geomeet.api.adapter.web.auth;

import com.geomeet.api.adapter.web.auth.dto.LoginRequest;
import com.geomeet.api.adapter.web.auth.dto.LoginResponse;
import com.geomeet.api.application.command.LoginCommand;
import com.geomeet.api.application.command.RegisterCommand;
import com.geomeet.api.application.result.LoginResult;
import com.geomeet.api.application.usecase.auth.LoginUseCase;
import com.geomeet.api.application.usecase.auth.RegisterUseCase;
import com.geomeet.api.infrastructure.security.JwtTokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final JwtTokenService jwtTokenService;
  private final RegisterUseCase registerUseCase;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginCommand command = LoginCommand.of(
        request.getUsernameOrEmail(),
        request.getPassword()
    );

    LoginResult result = loginUseCase.execute(command);

    String token = jwtTokenService.generateToken(result.getUserId(), result.getUsername());

    LoginResponse response = LoginResponse.toResponse(token, result);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/register")
  public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
    RegisterCommand command = RegisterCommand.of(request.getUsername(), request.getPassword(),
        request.getEmail(),
        request.getVerificationCode());

    LoginResult result = registerUseCase.execute(command);
    String token = jwtTokenService.generateToken(result.getUserId(), result.getUsername());

    LoginResponse response = LoginResponse.toResponse(token, result);
    return ResponseEntity.ok(response);
  }
}

