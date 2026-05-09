package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet summary response for the current authenticated user.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletResponse {

    private String userId;
    private BigDecimal balance;
    private BigDecimal pendingBalance;
    private BigDecimal totalEarned;
    private BigDecimal totalWithdrawn;
    private String currency;
    private LocalDateTime updatedAt;
}

