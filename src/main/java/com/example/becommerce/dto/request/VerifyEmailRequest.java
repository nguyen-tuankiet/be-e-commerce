package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for POST /api/auth/verify-email
 */
@Getter
@Setter
public class VerifyEmailRequest {

    @NotBlank(message = "Token không được để trống")
    private String token;
}

