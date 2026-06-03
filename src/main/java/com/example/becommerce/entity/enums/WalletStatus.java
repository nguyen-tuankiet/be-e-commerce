package com.example.becommerce.entity.enums;

import com.example.becommerce.constant.WalletConstant;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Computed wallet health tiers.
 */
public enum WalletStatus {
    NORMAL,
    LOW_BALANCE,
    LOCKED;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static WalletStatus fromBalance(BigDecimal balance) {
        return fromBalance(balance, WalletConstant.DEFAULT_MINIMUM_COMMISSION_BALANCE);
    }

    public static WalletStatus fromBalance(BigDecimal balance, BigDecimal minimumCommissionBalance) {
        BigDecimal safeBalance = balance == null ? BigDecimal.ZERO : balance;
        BigDecimal threshold = minimumCommissionBalance == null ? WalletConstant.DEFAULT_MINIMUM_COMMISSION_BALANCE : minimumCommissionBalance;

        if (safeBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return LOCKED;
        }
        if (safeBalance.compareTo(threshold) <= 0) {
            return LOW_BALANCE;
        }
        return NORMAL;
    }
}
