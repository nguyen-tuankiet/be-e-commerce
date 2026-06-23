package com.example.becommerce.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Result of the customer choosing a payment method for an order.
 *
 * <ul>
 *   <li>Cash: the order is finalized immediately, {@code completed = true} and
 *       {@code checkoutUrl} is {@code null}.</li>
 *   <li>VNPay: the order stays in {@code awaiting-payment} until the gateway IPN
 *       confirms; {@code completed = false} and {@code checkoutUrl} points to the
 *       VNPay payment page the customer must be redirected to.</li>
 * </ul>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderPaymentResponse {

    private String orderId;
    private String status;
    private String paymentMethod;
    private Long amount;

    /** True when the payment is already settled (cash flow). */
    private boolean completed;

    /** VNPay checkout URL the FE must redirect to; null for cash. */
    private String checkoutUrl;

    /** Wallet transaction code backing the VNPay payment; null for cash. */
    private String transactionId;
}
