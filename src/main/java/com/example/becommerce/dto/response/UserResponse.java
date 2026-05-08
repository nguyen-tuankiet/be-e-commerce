package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * User data returned in API responses.
 * Never exposes password or internal fields.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String code;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String avatar;
    private String district;
    private String address;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
