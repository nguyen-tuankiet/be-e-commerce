package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Commission settings response.
 * Retrieved via GET /api/admin/commission-settings
 * Contains the complete commission configuration including auto-lock settings.
 */
@Getter
@Builder
public class CommissionSettingsResponse {
    private final BigDecimal fixedCommissionFee;
    private final BigDecimal minimumCommissionBalance;
    private final Boolean autoLockEnabled;
    private final LocalDateTime updatedAt;
}
