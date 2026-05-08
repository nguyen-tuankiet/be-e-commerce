package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for PATCH /api/users/{id}/status
 * Admin-only endpoint.
 */
@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String reason;
}
