package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Full authentication response containing tokens + user info.
 */
@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
