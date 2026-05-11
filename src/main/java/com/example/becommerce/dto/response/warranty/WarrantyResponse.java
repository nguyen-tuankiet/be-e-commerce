package com.example.becommerce.dto.response.warranty;

import com.example.becommerce.dto.response.order.OrderPartySummary;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarrantyResponse {

    private String id;
    private String orderId;
    private String status;
    private String description;

    private List<String> images;

    private LocalDateTime scheduledAt;
    private LocalDateTime warrantyExpiresAt;
    private Long remainingDays;

    private OrderPartySummary technician;

    private LocalDateTime createdAt;
}
