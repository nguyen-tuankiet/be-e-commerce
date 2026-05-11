package com.example.becommerce.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderPriceAdjustmentResponse {

    private Long originalPrice;
    private Long newPrice;
    private String reason;
    private String status;
    private List<Part> parts;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;

    @Getter
    @Builder
    public static class Part {
        private String name;
        private Long price;
        private String partCode;
    }
}
