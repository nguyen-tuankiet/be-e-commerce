package com.example.becommerce.dto.request;

import com.example.becommerce.entity.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO để tạo order report/complaint
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReportRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Against ID is required")
    private Long againstId;

    @NotNull(message = "Reason is required")
    private ReportReason reason;

    private String description;
}
