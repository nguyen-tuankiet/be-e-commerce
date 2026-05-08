package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.UserMapper;
import com.example.becommerce.dto.request.UpdateUserRequest;
import com.example.becommerce.dto.request.UpdateUserStatusRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.UserResponse;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.UserStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.service.UserService;
import com.example.becommerce.utils.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementation of UserService.
 * Handles pagination, filtering, profile updates, and status management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    // ----------------------------------------------------------------
    // List users with filters + pagination
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsers(
            String role, String status, String district, String keyword, int page, int limit) {

        // Build specification
        Specification<User> spec = UserSpecification.buildFilter(role, status, district, keyword);

        // 0-indexed for Spring Data, but API is 1-indexed
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> items = userPage.getContent()
                .stream()
                .map(userMapper::toResponse)
                .toList();

        return PagedResponse.of(items, page, limit, userPage.getTotalElements());
    }

    // ----------------------------------------------------------------
    // Get single user
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findActiveUserById(id);
        return userMapper.toResponse(user);
    }

    // ----------------------------------------------------------------
    // Update profile
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findActiveUserById(id);

        // Email uniqueness check (if changing)
        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
                throw AppException.conflict(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã được sử dụng");
            }
            user.setEmail(request.getEmail().toLowerCase().trim());
        }

        // Phone uniqueness check (if changing)
        if (StringUtils.hasText(request.getPhone())
                && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhoneAndDeletedFalse(request.getPhone())) {
                throw AppException.conflict(ErrorCode.PHONE_ALREADY_EXISTS, "Số điện thoại đã được sử dụng");
            }
            user.setPhone(request.getPhone().trim());
        }

        // Apply optional fields
        if (StringUtils.hasText(request.getFullName()))  user.setFullName(request.getFullName());
        if (StringUtils.hasText(request.getAddress()))   user.setAddress(request.getAddress());
        if (StringUtils.hasText(request.getDistrict()))  user.setDistrict(request.getDistrict());
        if (StringUtils.hasText(request.getBio()))       user.setBio(request.getBio());
        if (StringUtils.hasText(request.getAvatar()))    user.setAvatar(request.getAvatar());

        user = userRepository.save(user);
        log.info("User profile updated: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    // ----------------------------------------------------------------
    // Update status (Admin only)
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = findActiveUserById(id);

        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS,
                    "Trạng thái không hợp lệ. Chỉ chấp nhận: PENDING, ACTIVE, LOCKED, INACTIVE");
        }

        user.setStatus(newStatus);
        user = userRepository.save(user);

        log.info("User [{}] status changed to {} by admin. Reason: {}",
                user.getCode(), newStatus, request.getReason());

        return userMapper.toResponse(user);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private User findActiveUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng với id: " + id));
    }
}
