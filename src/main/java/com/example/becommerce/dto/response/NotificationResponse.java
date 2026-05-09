package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Notification item response.
 */
@Getter
@Builder
public class NotificationResponse {

    private String id;
    private String type;
    private String title;
    private String body;
    private Map<String, Object> data;
    private boolean isRead;
    private LocalDateTime createdAt;
}

