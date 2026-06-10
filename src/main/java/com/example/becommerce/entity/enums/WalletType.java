package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Distinguishes which wallet bucket a transaction belongs to.
 */
public enum WalletType {
    CREDIT,
    PERSONAL;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
