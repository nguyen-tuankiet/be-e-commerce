package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Reasons a customer can use when reporting an order.
 * API uses snake_case ("extra_fee"); enum is uppercase.
 */
public enum ReportReason {
    EXTRA_FEE,
    BAD_ATTITUDE,
    NO_SHOW,
    POOR_QUALITY,
    FRAUD,
    OTHER;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ReportReason from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ReportReason.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
