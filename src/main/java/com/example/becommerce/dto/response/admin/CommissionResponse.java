package com.example.becommerce.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CommissionResponse {
    private final BigDecimal platformFeePercent;
    private final BigDecimal vatPercent;
    private final String updatedBy;
    private final LocalDateTime updatedAt;
}
