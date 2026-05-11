package com.example.becommerce.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WalletAdjustRequest {
    @NotBlank
    private String technicianId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String type;

    @NotBlank
    private String reason;
}
