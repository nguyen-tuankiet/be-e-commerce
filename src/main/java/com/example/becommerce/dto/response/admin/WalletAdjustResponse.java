package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WalletAdjustResponse {
    private final String transactionId;
    private final String technicianId;
    private final BigDecimal amount;
    private final BigDecimal newBalance;
    private final String reason;
    private final LocalDateTime createdAt;
}
