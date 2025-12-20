package com.geomeet.api.adapter.web.auth.dto;

import com.geomeet.api.application.result.LoginResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String username;
    private String email;
    private String message;

    public static LoginResponse toResponse(String token, LoginResult result){
       return LoginResponse.builder()
            .token(token)
            .username(result.getUsername())
            .email(result.getEmail())
            .message("Login successful")
            .build();
    }
}

