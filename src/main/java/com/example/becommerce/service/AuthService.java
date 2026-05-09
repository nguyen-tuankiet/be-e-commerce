package com.example.becommerce.service;

import com.example.becommerce.dto.request.*;
import com.example.becommerce.dto.response.AuthResponse;
import com.example.becommerce.dto.response.TokenRefreshResponse;
import com.example.becommerce.dto.response.UserResponse;

/**
 * Auth service contract — defines all authentication operations.
 */
public interface AuthService {

    /** Register new user and return tokens + user info. */
    AuthResponse register(RegisterRequest request);

    /** Login by email or phone, check role, return tokens. */
    AuthResponse login(LoginRequest request);

    /** Validate refresh token and issue new access token. */
    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    /** Invalidate all refresh tokens for current user. */
    void logout(String bearerToken);

    /** Generate and send (or log) a password reset token. */
    void forgotPassword(ForgotPasswordRequest request);

    /** Verify reset token and update password. */
    void changePassword(ChangePasswordRequest request);

    /** Return current authenticated user's profile. */
    UserResponse getCurrentUser(String bearerToken);

    /** Verify email with confirmation token. */
    void verifyEmail(VerifyEmailRequest request);
}
