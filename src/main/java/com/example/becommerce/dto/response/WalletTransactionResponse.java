package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Item response for wallet transaction history.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletTransactionResponse {

    private String id;
    private String type;
    private String title;
    private String category;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}

