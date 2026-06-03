package com.example.becommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request body for POST /api/wallet/topup
 */
@Getter
@Setter
public class WalletTopUpRequest {

    @NotNull(message = "Số tiền nạp không được để trống")
    @DecimalMin(value = "10000", message = "Số tiền nạp tối thiểu là 10,000")
    @Digits(integer = 19, fraction = 0, message = "Số tiền nạp phải là số nguyên")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Pattern(regexp = "(?i)^vnpay$", message = "Hiện tại chỉ hỗ trợ thanh toán bằng VNPay")
    private String method;
}




