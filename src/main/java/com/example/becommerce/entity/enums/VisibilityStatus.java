package com.example.becommerce.entity.enums;

import java.util.Locale;

public enum VisibilityStatus {
    ACTIVE,
    INACTIVE;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static VisibilityStatus from(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        return VisibilityStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
