package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Lifecycle of a warranty claim raised by the customer after an order is completed.
 */
public enum WarrantyStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    EXPIRED;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static WarrantyStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return WarrantyStatus.valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}
