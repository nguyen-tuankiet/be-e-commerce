package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Wallet transaction types.
 */
public enum TransactionType {
    TOPUP,
    WITHDRAW,
    COMMISSION,
    PAYMENT,
    REFUND;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static TransactionType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TransactionType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

