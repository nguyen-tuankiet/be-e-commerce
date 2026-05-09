package com.example.becommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request body for POST /api/wallet/withdraw
 */
@Getter
@Setter
public class WalletWithdrawRequest {

    @NotNull(message = "Số tiền rút không được để trống")
    @DecimalMin(value = "50000", message = "Số tiền rút tối thiểu là 50,000")
    @Digits(integer = 19, fraction = 0, message = "Số tiền rút phải là số nguyên")
    private BigDecimal amount;

    @NotBlank(message = "Tài khoản ngân hàng không được để trống")
    private String bankAccountId;
}


