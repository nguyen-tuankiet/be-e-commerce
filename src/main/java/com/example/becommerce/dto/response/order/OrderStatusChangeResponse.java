package com.example.becommerce.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Lightweight response for status transition endpoints (cancel, accept, complete, ...).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusChangeResponse {

    private String id;
    private String status;

    private OrderPartySummary technician;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private String cancelReason;

    private Long finalPrice;

    private LocalDateTime updatedAt;

    /** Free-form message for transitions that need it (e.g. reject -> pool). */
    private String message;
}
