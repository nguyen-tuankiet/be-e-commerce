package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Notification categories used for user inbox.
 */
public enum NotificationType {
    ORDER_ACCEPTED,
    PRICE_ADJUSTMENT,
    ORDER_COMPLETED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    WITHDRAW_SUCCESS,
    WITHDRAW_FAILED,
    SYSTEM,
    PROMOTION,
    CHAT_MESSAGE;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static NotificationType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return NotificationType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

