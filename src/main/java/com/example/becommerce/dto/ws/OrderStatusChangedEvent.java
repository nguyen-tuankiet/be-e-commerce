package com.example.becommerce.dto.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Pushed to {@code /topic/orders.{orderCode}} on any state transition
 * (cancel, accept, reject, in_progress, complete).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusChangedEvent {

    @Builder.Default
    private final String event = "order:status_changed";

    private String orderId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime updatedAt;
}
