package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private String code;
    private String status;

    private Long customerId;
    private String customerName;
    private String customerPhone;

    private Long technicianId;
    private String technicianName;
    private String technicianPhone;

    private String serviceName;
    private String description;
    private String address;
    private String district;
    private BigDecimal totalPrice;
    private String note;
    private String cancelReason;
    private BigDecimal extraCost;
    private String extraCostReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
