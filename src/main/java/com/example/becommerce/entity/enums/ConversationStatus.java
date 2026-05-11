package com.example.becommerce.entity.enums;

import java.util.Locale;

public enum ConversationStatus {
    ACTIVE,
    ARCHIVED,
    CLOSED;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ConversationStatus from(String value) {
        if (value == null || value.isBlank()) return null;
        return ConversationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
