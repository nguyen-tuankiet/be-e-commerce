package com.example.becommerce.entity.enums;

/**
 * User account statuses.
 * - PENDING: newly registered, awaiting approval
 * - ACTIVE: approved and fully operational
 * - LOCKED: suspended by admin
 * - INACTIVE: self-deactivated or system-deactivated
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    LOCKED,
    INACTIVE
}
