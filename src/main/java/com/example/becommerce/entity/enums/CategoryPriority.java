package com.example.becommerce.entity.enums;

import java.util.Locale;

public enum CategoryPriority {
    LOW,
    NORMAL,
    HIGH;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CategoryPriority from(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }
        return CategoryPriority.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
