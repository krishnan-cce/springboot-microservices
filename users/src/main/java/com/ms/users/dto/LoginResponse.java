package com.ms.users.dto;

import lombok.*;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserResponse user;
}