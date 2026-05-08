package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for POST /api/auth/forgot-password
 */
@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String identifier;
}
