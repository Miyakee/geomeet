package com.geomeet.api.adapter.web.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegisterRequest {
  @NotEmpty
  private String username;
  @NotEmpty
  private String password;
  @NotEmpty
  private String email;
  @NotEmpty
  private String verificationCode;
}
