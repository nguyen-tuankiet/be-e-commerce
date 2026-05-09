package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.constant.NotificationConstant;
import com.example.becommerce.dto.mapper.NotificationMapper;
import com.example.becommerce.dto.response.MarkReadResponse;
import com.example.becommerce.dto.response.NotificationListResponse;
import com.example.becommerce.dto.response.NotificationResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.ReadAllNotificationResponse;
import com.example.becommerce.entity.Notification;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.NotificationType;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.NotificationRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.service.NotificationService;
import com.example.becommerce.utils.JsonUtils;
import com.example.becommerce.utils.NotificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Notification service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final int CODE_GENERATION_RETRY_LIMIT = 10;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationCodeGenerator notificationCodeGenerator;
    private final JsonUtils jsonUtils;

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse getMyNotifications(int page, int limit) {
        User currentUser = getCurrentUser();

        int safePage = Math.max(NotificationConstant.DEFAULT_PAGE, page);
        int safeLimit = Math.max(1, Math.min(limit, NotificationConstant.MAX_LIMIT));

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage = notificationRepository.findByUser_Id(currentUser.getId(), pageable);

        List<NotificationResponse> items = notificationPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .toList();

        long unreadCount = notificationRepository.countByUser_IdAndIsReadFalse(currentUser.getId());

        return NotificationListResponse.builder()
                .unreadCount(unreadCount)
                .items(items)
                .pagination(PagedResponse.PaginationMeta.builder()
                        .page(safePage)
                        .limit(safeLimit)
                        .total(notificationPage.getTotalElements())
                        .totalPages(notificationPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public MarkReadResponse markAsRead(String id) {
        if (!StringUtils.hasText(id)) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Mã thông báo không hợp lệ");
        }

        User currentUser = getCurrentUser();
        Notification notification = notificationRepository
                .findByNotificationCodeAndUser_Id(id.trim(), currentUser.getId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.NOTIFICATION_NOT_FOUND,
                        "Không tìm thấy thông báo",
                        HttpStatus.NOT_FOUND));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return MarkReadResponse.builder()
                .id(notification.getNotificationCode())
                .isRead(true)
                .build();
    }

    @Override
    @Transactional
    public ReadAllNotificationResponse markAllAsRead() {
        User currentUser = getCurrentUser();
        int updatedCount = notificationRepository.markAllAsRead(currentUser.getId(), LocalDateTime.now());

        return ReadAllNotificationResponse.builder()
                .updatedCount(updatedCount)
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(
            User user,
            NotificationType type,
            String title,
            String body,
            Map<String, Object> data) {

        if (user == null || user.getId() == null) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Người nhận thông báo không hợp lệ");
        }
        if (type == null) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Loại thông báo là bắt buộc");
        }
        if (!StringUtils.hasText(title) || !StringUtils.hasText(body)) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Tiêu đề và nội dung thông báo là bắt buộc");
        }

        String jsonPayload = jsonUtils.toJson(data);

        for (int attempt = 0; attempt < CODE_GENERATION_RETRY_LIMIT; attempt++) {
            String notificationCode = generateCandidateCode(attempt);

            Notification notification = Notification.builder()
                    .notificationCode(notificationCode)
                    .user(user)
                    .type(type)
                    .title(title.trim())
                    .body(body.trim())
                    .dataJson(jsonPayload)
                    .isRead(false)
                    .build();

            try {
                Notification saved = notificationRepository.save(notification);
                return notificationMapper.toResponse(saved);
            } catch (DataIntegrityViolationException ex) {
                log.warn("Notification code collision detected for code {}", notificationCode);
            }
        }

        throw new AppException(
                ErrorCode.NOTIFICATION_CREATE_FAILED,
                "Không thể tạo thông báo tại thời điểm hiện tại",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String generateCandidateCode(int offset) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long nextSequence = notificationRepository.countByCreatedAtBetween(startOfDay, endOfDay) + 1 + offset;
        return notificationCodeGenerator.generate(today, nextSequence);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw AppException.unauthorized("Người dùng chưa đăng nhập");
        }

        String email = authentication.getName();
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng hiện tại"));
    }
}

