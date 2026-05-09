package com.example.becommerce.utils;

/**
 * Masks bank account numbers for safe API responses.
 */
public final class BankAccountMaskUtils {

    private BankAccountMaskUtils() {}

    public static String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return accountNumber;
        }

        String normalized = accountNumber.replaceAll("\\s+", "");
        if (normalized.length() <= 7) {
            return "****";
        }

        int prefixLength = normalized.length() > 10 ? 6 : 4;
        String prefix = normalized.substring(0, Math.min(prefixLength, normalized.length() - 3));
        String suffix = normalized.substring(normalized.length() - 3);
        return prefix + " **** " + suffix;
    }
}


