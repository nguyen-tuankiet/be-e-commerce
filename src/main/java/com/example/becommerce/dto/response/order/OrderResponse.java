package com.example.becommerce.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full order detail. Some fields may be omitted on list views
 * via the OrderMapper.toListItem variant.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private String id;
    private String status;

    private String serviceName;
    private String subService;
    private String serviceCategory;
    private String deviceName;
    private String description;
    private String address;

    private LocalDateTime scheduledAt;
    private LocalDateTime expectedTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private Long estimatedPrice;
    private Long finalPrice;
    private String paymentMethod;
    private Integer warrantyMonths;

    private OrderPartySummary customer;
    private OrderPartySummary technician;

    private OrderPriceAdjustmentResponse priceAdjustment;

    private List<String> images;

    private String cancelledBy;
    private String cancelReason;
    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
