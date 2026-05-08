package com.example.becommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for POST /api/auth/register
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3-9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    /**
     * Password must contain at least 8 characters, one uppercase,
     * one lowercase, one digit, and one special character.
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;

    /**
     * Only CUSTOMER or TECHNICIAN allowed at registration.
     * Must match case-insensitively.
     */
    @NotBlank(message = "Vai trò không được để trống")
    @Pattern(
        regexp = "(?i)^(customer|technician)$",
        message = "Vai trò chỉ được là CUSTOMER hoặc TECHNICIAN"
    )
    private String role;
}
