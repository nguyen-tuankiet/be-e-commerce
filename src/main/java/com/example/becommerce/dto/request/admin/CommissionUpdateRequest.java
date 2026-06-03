package com.example.becommerce.dto.request.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Commission update request.
 * 
 * Backward compatibility notes:
 * - Removed: platformFeePercent, vatPercent (moved to AdminSettings)
 * - autoLockEnabled is now retrieved via GET /api/admin/commission-settings
 */
@Getter
@Setter
public class CommissionUpdateRequest {
    @NotNull(message = "Fixed commission fee is required")
    @DecimalMin(value = "0", message = "Fixed commission fee must be greater than or equal to 0")
    private BigDecimal fixedCommissionFee;

    @NotNull(message = "Minimum commission balance is required")
    @DecimalMin(value = "0", message = "Minimum commission balance must be greater than or equal to 0")
    private BigDecimal minimumCommissionBalance;
}
