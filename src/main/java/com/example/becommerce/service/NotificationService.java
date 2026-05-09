package com.example.becommerce.service;

import com.example.becommerce.dto.response.MarkReadResponse;
import com.example.becommerce.dto.response.NotificationListResponse;
import com.example.becommerce.dto.response.NotificationResponse;
import com.example.becommerce.dto.response.ReadAllNotificationResponse;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.NotificationType;

import java.util.Map;

/**
 * Notification service contract.
 */
public interface NotificationService {

    NotificationListResponse getMyNotifications(int page, int limit);

    MarkReadResponse markAsRead(String id);

    ReadAllNotificationResponse markAllAsRead();

    NotificationResponse createNotification(
            User user,
            NotificationType type,
            String title,
            String body,
            Map<String, Object> data);
}

