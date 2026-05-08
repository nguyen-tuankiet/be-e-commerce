package com.example.becommerce.service;

import com.example.becommerce.dto.request.UpdateUserRequest;
import com.example.becommerce.dto.request.UpdateUserStatusRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.UserResponse;

/**
 * User management service contract.
 */
public interface UserService {

    /**
     * Paginated, filterable list of users.
     *
     * @param role     optional role filter
     * @param status   optional status filter
     * @param district optional district filter
     * @param keyword  partial match on fullName or email
     * @param page     1-based page number
     * @param limit    page size
     */
    PagedResponse<UserResponse> getUsers(
            String role, String status, String district, String keyword, int page, int limit);

    /** Get a single user by id. */
    UserResponse getUserById(Long id);

    /** Update user profile fields. */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /** Admin-only: update user status with optional reason. */
    UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request);
}
