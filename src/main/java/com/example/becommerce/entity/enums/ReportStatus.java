package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Lifecycle of an order report once it has been filed.
 */
public enum ReportStatus {
    OPEN,
    INVESTIGATING,
    RESOLVED,
    DISMISSED;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ReportStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ReportStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
