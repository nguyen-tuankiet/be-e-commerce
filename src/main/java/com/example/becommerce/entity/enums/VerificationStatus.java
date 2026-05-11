package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * KYC verification status for a technician.
 * Distinct from {@link UserStatus} which controls account access.
 */
public enum VerificationStatus {
    NONE,
    PENDING,
    APPROVED,
    REJECTED;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static VerificationStatus from(String value) {
        if (value == null || value.isBlank()) return null;
        return VerificationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
