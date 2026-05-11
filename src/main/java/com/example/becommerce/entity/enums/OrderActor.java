package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Who performed an order action.
 * Used for status history and cancellation attribution.
 */
public enum OrderActor {
    CUSTOMER,
    TECHNICIAN,
    ADMIN,
    SYSTEM;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static OrderActor from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OrderActor.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
