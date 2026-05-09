package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for POST /api/wallet/topup/confirm
 */
@Getter
@Setter
public class WalletTopUpConfirmRequest {

    @NotBlank(message = "Mã giao dịch không được để trống")
    private String transactionId;
}
