package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Marketing tier of a technician — drives the "type" and badge fields
 * shown in the technician listings.
 */
public enum TechnicianTier {
    NORMAL,
    PREMIUM;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static TechnicianTier from(String value) {
        if (value == null || value.isBlank()) return NORMAL;
        return TechnicianTier.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
