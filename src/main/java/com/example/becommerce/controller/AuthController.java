package com.example.becommerce.controller;

import com.example.becommerce.constant.ApiConstant;
import com.example.becommerce.dto.request.*;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.AuthResponse;
import com.example.becommerce.dto.response.TokenRefreshResponse;
import com.example.becommerce.dto.response.UserResponse;
import com.example.becommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 * All endpoints are public — no authentication required.
 * Base path: /api/auth
 */
@RestController
@RequestMapping(ApiConstant.AUTH_BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ----------------------------------------------------------------
    // POST /api/auth/register
    // ----------------------------------------------------------------

    @PostMapping(ApiConstant.AUTH_REGISTER)
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    // ----------------------------------------------------------------
    // POST /api/auth/login
    // ----------------------------------------------------------------

    @PostMapping(ApiConstant.AUTH_LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ----------------------------------------------------------------
    // POST /api/auth/refresh-token
    // ----------------------------------------------------------------

    @PostMapping(ApiConstant.AUTH_REFRESH)
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        TokenRefreshResponse data = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ----------------------------------------------------------------
    // POST /api/auth/logout
    // ----------------------------------------------------------------

    @PostMapping(ApiConstant.AUTH_LOGOUT)
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {

        authService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ----------------------------------------------------------------
    // POST /api/auth/forgot-password
    // ----------------------------------------------------------------

    @PostMapping(ApiConstant.AUTH_FORGOT_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ----------------------------------------------------------------
    // POST /api/auth/change-password
    // ----------------------------------------------------------------

    @PostMapping(ApiConstant.AUTH_CHANGE_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ----------------------------------------------------------------
    // GET /api/auth/me  (requires Bearer token)
    // ----------------------------------------------------------------

    @GetMapping(ApiConstant.AUTH_ME)
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        UserResponse data = authService.getCurrentUser(authHeader);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ----------------------------------------------------------------
    // POST /api/auth/verify-email
    // ----------------------------------------------------------------

    @PostMapping(ApiConstant.AUTH_VERIFY_EMAIL)
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
