package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Lifecycle of a price adjustment request raised by the technician.
 */
public enum PriceAdjustmentStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static PriceAdjustmentStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return PriceAdjustmentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
