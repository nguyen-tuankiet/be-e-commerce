package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Wallet transaction processing statuses.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    AWAITING_PAYMENT,
    PENDING_VERIFICATION;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static TransactionStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TransactionStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

