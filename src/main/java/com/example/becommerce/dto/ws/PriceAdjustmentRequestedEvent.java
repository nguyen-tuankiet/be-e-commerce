package com.example.becommerce.dto.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Pushed to {@code /topic/orders.{orderCode}} when the technician
 * requests a price change on an order.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceAdjustmentRequestedEvent {

    @Builder.Default
    private final String event = "price:adjustment_requested";

    private String orderId;
    private Long originalPrice;
    private Long newPrice;
    private String reason;
}
