package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.UserResponse;
import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

/**
 * Manual mapper between User entity and UserResponse DTO.
 * Keeps entity details out of the response layer.
 */
@Component
public class UserMapper {

    /**
     * Convert a User entity to a UserResponse DTO.
     */
    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .code(user.getCode())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name().toLowerCase())
                .status(user.getStatus().name().toLowerCase())
                .avatar(user.getAvatar())
                .district(user.getDistrict())
                .address(user.getAddress())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
