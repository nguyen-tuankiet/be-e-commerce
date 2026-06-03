package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for creating a top-up transaction.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletTopUpResponse {

    private String transactionId;
    private BigDecimal amount;
    private String method;
    private String checkoutUrl;
    private String deepLink;
    private String qrCodeUrl;
    private PaymentInfo paymentInfo;
    private LocalDateTime expiredAt;
    private String status;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentInfo {
        private String bankName;
        private String accountName;
        private String accountNumber;
        private String transferContent;
        private String qrCode;
    }
}


