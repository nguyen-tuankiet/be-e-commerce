package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Response for confirming a top-up transaction.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletTopUpConfirmResponse {

    private String transactionId;
    private String status;
    private String message;
}

