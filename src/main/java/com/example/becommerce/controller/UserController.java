package com.example.becommerce.controller;

import com.example.becommerce.constant.ApiConstant;
import com.example.becommerce.dto.request.UpdateUserRequest;
import com.example.becommerce.dto.request.UpdateUserStatusRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.UserResponse;
import com.example.becommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User management controller.
 * All endpoints require authentication; status endpoint requires ADMIN role.
 * Base path: /api/users
 */
@RestController
@RequestMapping(ApiConstant.USER_BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ----------------------------------------------------------------
    // GET /api/users?role=&status=&district=&keyword=&page=1&limit=10
    // ----------------------------------------------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit) {

        PagedResponse<UserResponse> data =
                userService.getUsers(role, status, district, keyword, page, limit);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ----------------------------------------------------------------
    // GET /api/users/{id}
    // ----------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse data = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ----------------------------------------------------------------
    // PATCH /api/users/{id}
    // ----------------------------------------------------------------

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse data = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ----------------------------------------------------------------
    // PATCH /api/users/{id}/status  — ADMIN only
    // ----------------------------------------------------------------

    @PatchMapping(ApiConstant.USER_UPDATE_STATUS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        UserResponse data = userService.updateUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
