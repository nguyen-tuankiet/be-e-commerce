package com.example.becommerce.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Envelope returned by price adjustment endpoints, matching the
 * { "id": "GU-...", "priceAdjustment": {...} } shape from the spec.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceAdjustmentEnvelope {

    private String id;
    private OrderPriceAdjustmentResponse priceAdjustment;
}
