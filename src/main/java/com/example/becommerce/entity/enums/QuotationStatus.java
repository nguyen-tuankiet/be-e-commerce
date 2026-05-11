package com.example.becommerce.entity.enums;

import java.util.Locale;

public enum QuotationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static QuotationStatus from(String value) {
        if (value == null || value.isBlank()) return null;
        return QuotationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
