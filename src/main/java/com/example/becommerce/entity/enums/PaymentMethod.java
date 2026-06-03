package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Supported wallet payment methods.
 */
public enum PaymentMethod {
    VIETQR,
    VNPAY,
    MOMO,
    BANK_TRANSFER;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static PaymentMethod from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return PaymentMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}


