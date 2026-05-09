package com.example.becommerce.controller;

import com.example.becommerce.constant.ApiConstant;
import com.example.becommerce.constant.NotificationConstant;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.MarkReadResponse;
import com.example.becommerce.dto.response.NotificationListResponse;
import com.example.becommerce.dto.response.ReadAllNotificationResponse;
import com.example.becommerce.service.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification inbox controller for authenticated users.
 */
@RestController
@RequestMapping(ApiConstant.NOTIFICATION_BASE)
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getMyNotifications(
            @RequestParam(defaultValue = "" + NotificationConstant.DEFAULT_PAGE) @Min(1) int page,
            @RequestParam(defaultValue = "" + NotificationConstant.DEFAULT_LIMIT) @Min(1) @Max(NotificationConstant.MAX_LIMIT) int limit) {

        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(page, limit)));
    }

    @PatchMapping(ApiConstant.NOTIFICATION_MARK_READ)
    public ResponseEntity<ApiResponse<MarkReadResponse>> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(id)));
    }

    @PatchMapping(ApiConstant.NOTIFICATION_MARK_ALL_READ)
    public ResponseEntity<ApiResponse<ReadAllNotificationResponse>> markAllAsRead() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAllAsRead()));
    }
}


