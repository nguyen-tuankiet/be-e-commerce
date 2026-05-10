package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request DTO để tạo warranty claim
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyClaimRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    private String description;

    @NotNull(message = "Warranty days is required")
    @Positive(message = "Warranty days must be positive")
    private Long warrantyDays;
}
