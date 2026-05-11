package com.example.becommerce.dto.request.admin;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CommissionUpdateRequest {
    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal platformFeePercent;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal vatPercent;
}
