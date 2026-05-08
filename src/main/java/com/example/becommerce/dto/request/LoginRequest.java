package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for POST /api/auth/login
 * Accepts email or phone as identifier.
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String identifier;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    /**
     * Optional role check — ensures user is logging in with expected role.
     * If provided, must match the user's actual role.
     */
    private String role;
}
