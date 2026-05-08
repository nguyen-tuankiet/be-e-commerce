package com.example.becommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for PATCH /api/users/{id}
 * All fields are optional — only provided fields are updated.
 */
@Getter
@Setter
public class UpdateUserRequest {

    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[3-9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String address;

    private String district;

    private String bio;

    private String avatar;
}
