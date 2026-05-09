package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.UserMapper;
import com.example.becommerce.dto.request.*;
import com.example.becommerce.dto.response.AuthResponse;
import com.example.becommerce.dto.response.TokenRefreshResponse;
import com.example.becommerce.dto.response.UserResponse;
import com.example.becommerce.entity.PasswordResetToken;
import com.example.becommerce.entity.RefreshToken;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.entity.enums.UserStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.PasswordResetTokenRepository;
import com.example.becommerce.repository.RefreshTokenRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.security.JwtProvider;
import com.example.becommerce.service.AuthService;
import com.example.becommerce.service.WalletService;
import com.example.becommerce.utils.UserCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of AuthService.
 * Handles register, login, token refresh, logout, forgot/change password, and me.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository              userRepository;
    private final RefreshTokenRepository      refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder             passwordEncoder;
    private final JwtProvider                 jwtProvider;
    private final UserMapper                  userMapper;
    private final UserCodeGenerator           codeGenerator;
    private final WalletService               walletService;

    // ----------------------------------------------------------------
    // Register
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Uniqueness checks
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw AppException.conflict(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã được sử dụng");
        }
        if (userRepository.existsByPhoneAndDeletedFalse(request.getPhone())) {
            throw AppException.conflict(ErrorCode.PHONE_ALREADY_EXISTS, "Số điện thoại đã được sử dụng");
        }

        // 2. Parse role — only CUSTOMER or TECHNICIAN allowed at registration
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
            if (role == Role.ADMIN) {
                throw AppException.badRequest(ErrorCode.ROLE_NOT_ALLOWED, "Vai trò không được phép đăng ký");
            }
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest(ErrorCode.ROLE_NOT_ALLOWED, "Vai trò không hợp lệ");
        }

        // 3. Generate unique user code
        long count = userRepository.countAll();
        String code = codeGenerator.generate(count + 1);

        // 4. Build and save user
        User user = User.builder()
                .code(code)
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.PENDING)
                .build();

        user = userRepository.save(user);
        walletService.createWalletForUser(user);
        log.info("New user registered: {} [{}]", user.getEmail(), user.getCode());

        // 5. Issue tokens
        return buildAuthResponse(user);
    }

    // ----------------------------------------------------------------
    // Login
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Find user by email or phone
        User user = userRepository.findByEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> AppException.badRequest(
                        ErrorCode.INVALID_CREDENTIALS, "Email/Số điện thoại hoặc mật khẩu không đúng"));

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw AppException.badRequest(ErrorCode.INVALID_CREDENTIALS,
                    "Email/Số điện thoại hoặc mật khẩu không đúng");
        }

        // 3. Optional role check
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                Role expectedRole = Role.valueOf(request.getRole().toUpperCase());
                if (user.getRole() != expectedRole) {
                    throw AppException.badRequest(ErrorCode.INVALID_CREDENTIALS,
                            "Tài khoản không tồn tại với vai trò này");
                }
            } catch (IllegalArgumentException e) {
                throw AppException.badRequest(ErrorCode.ROLE_NOT_ALLOWED, "Vai trò không hợp lệ");
            }
        }

        // 4. Check account status
        if (user.getStatus() == UserStatus.LOCKED) {
            throw AppException.badRequest(ErrorCode.ACCOUNT_LOCKED, "Tài khoản đã bị khóa");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw AppException.badRequest(ErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa");
        }

        // 5. Issue tokens
        return buildAuthResponse(user);
    }

    // ----------------------------------------------------------------
    // Refresh Token
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();

        // 1. Validate JWT signature/expiry
        if (!jwtProvider.isTokenValid(rawToken)) {
            throw AppException.badRequest(ErrorCode.INVALID_TOKEN, "Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // 2. Check DB record — must not be revoked
        RefreshToken dbToken = refreshTokenRepository.findByTokenAndRevokedFalse(rawToken)
                .orElseThrow(() -> AppException.badRequest(ErrorCode.TOKEN_REVOKED, "Refresh token đã bị thu hồi"));

        // 3. Check expiry in DB
        if (dbToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw AppException.badRequest(ErrorCode.TOKEN_EXPIRED, "Refresh token đã hết hạn");
        }

        // 4. Issue new access token
        User user = dbToken.getUser();
        Map<String, Object> claims = buildClaims(user);
        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail(), claims);

        log.info("Access token refreshed for user: {}", user.getEmail());
        return TokenRefreshResponse.builder().accessToken(newAccessToken).build();
    }

    // ----------------------------------------------------------------
    // Logout
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public void logout(String bearerToken) {
        String token = extractBearerToken(bearerToken);

        if (!jwtProvider.isTokenValid(token)) {
            // Token already invalid — treat as logged out
            return;
        }

        String email = jwtProvider.extractEmail(token);
        userRepository.findByEmailAndDeletedFalse(email).ifPresent(user -> {
            refreshTokenRepository.revokeAllByUserId(user.getId());
            log.info("User logged out — all refresh tokens revoked: {}", email);
        });
    }

    // ----------------------------------------------------------------
    // Forgot Password
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy tài khoản với thông tin này"));

        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.invalidateAllByUserId(user.getId());

        // Generate new reset token
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiredAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // TODO: Send email/SMS with the reset link
        // For now, log the token (replace with real email service in production)
        log.info("Password reset token for {}: {}", user.getEmail(), tokenValue);
    }

    // ----------------------------------------------------------------
    // Change Password
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Validate confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw AppException.badRequest(ErrorCode.PASSWORDS_DO_NOT_MATCH, "Mật khẩu xác nhận không khớp");
        }

        // 2. Find valid reset token
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> AppException.badRequest(ErrorCode.INVALID_TOKEN, "Token không hợp lệ"));

        // 3. Check expiry
        if (resetToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw AppException.badRequest(ErrorCode.TOKEN_EXPIRED, "Token đã hết hạn");
        }

        // 4. Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 5. Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // 6. Revoke all refresh tokens — force re-login
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    // ----------------------------------------------------------------
    // Get Current User
    // ----------------------------------------------------------------

    @Override
    public UserResponse getCurrentUser(String bearerToken) {
        String token = extractBearerToken(bearerToken);
        String email = jwtProvider.extractEmail(token);

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng"));

        return userMapper.toResponse(user);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /**
     * Build and persist a refresh token, then return full AuthResponse.
     */
    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = buildClaims(user);
        String accessToken  = jwtProvider.generateAccessToken(user.getEmail(), claims);
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        // Persist refresh token in DB
        long expirationMs = jwtProvider.getRefreshTokenExpiration();
        RefreshToken dbToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiredAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(dbToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toResponse(user))
                .build();
    }

    /** Build JWT claims from user entity. */
    private Map<String, Object> buildClaims(User user) {
        return Map.of(
                "userId", user.getId(),
                "role",   user.getRole().name(),
                "code",   user.getCode()
        );
    }

    /** Strip "Bearer " prefix from Authorization header value. */
    private String extractBearerToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
