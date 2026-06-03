package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Response for withdrawal creation.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletWithdrawResponse {

    private String transactionId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private BankAccountInfo bankAccount;
    private String status;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BankAccountInfo {
        private String bankName;
        private String accountNumber;
        private String owner;
    }
}

