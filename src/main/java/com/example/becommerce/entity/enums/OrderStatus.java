package com.example.becommerce.entity.enums;

import java.util.Locale;

/**
 * Lifecycle states of an order.
 * - NEW          : just created, no technician assigned yet
 * - ASSIGNED     : technician accepted the order (alternative to SCHEDULED)
 * - SCHEDULED    : technician accepted, scheduled time fixed
 * - IN_PROGRESS  : technician started working
 * - COMPLETED    : finished and paid
 * - CANCELLED    : cancelled by customer / system / admin
 */
public enum OrderStatus {
    NEW,
    ASSIGNED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    /** API representation, e.g. IN_PROGRESS -> "in-progress" */
    public String apiValue() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static OrderStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OrderStatus.valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}
