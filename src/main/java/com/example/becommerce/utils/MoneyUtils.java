package com.example.becommerce.utils;

import com.example.becommerce.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money helpers for VND values.
 */
public final class MoneyUtils {

    private MoneyUtils() {}

    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(0, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal safeZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : normalize(amount);
    }

    public static BigDecimal displayAmount(TransactionType type, BigDecimal amount) {
        BigDecimal normalized = safeZero(amount);
        return switch (type) {
            case COMMISSION, PAYMENT, WITHDRAW -> normalized.negate();
            default -> normalized;
        };
    }

    public static String buildCategory(TransactionType type) {
        return switch (type) {
            case TOPUP -> "NẠP TIỀN";
            case WITHDRAW -> "RÚT TIỀN";
            case COMMISSION -> "DỊCH VỤ/HOA HỒNG";
            case PAYMENT -> "THANH TOÁN";
            case REFUND -> "HOÀN TIỀN";
        };
    }

    public static String generateTransferContent(String transferPrefix, BigDecimal amount) {
        return transferPrefix + " - " + safeZero(amount).toPlainString();
    }
}

