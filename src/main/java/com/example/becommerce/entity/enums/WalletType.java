package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Logical wallet pockets owned by a technician.
 */
public enum WalletType {
    CREDIT,
    PERSONAL;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static WalletType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return WalletType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
