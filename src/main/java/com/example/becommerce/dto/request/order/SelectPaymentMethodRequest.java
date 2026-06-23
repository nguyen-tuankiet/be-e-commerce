package com.example.becommerce.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Customer's choice of payment method for an order that is awaiting payment.
 * Accepted values: {@code "cash"} or {@code "vnpay"}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectPaymentMethodRequest {

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String method;
}
