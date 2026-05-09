package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.NotificationResponse;
import com.example.becommerce.entity.Notification;
import com.example.becommerce.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps notification entities to API responses.
 */
@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final JsonUtils jsonUtils;

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getNotificationCode())
                .type(notification.getType() != null ? notification.getType().apiValue() : null)
                .title(notification.getTitle())
                .body(notification.getBody())
                .data(jsonUtils.toMap(notification.getDataJson()))
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

