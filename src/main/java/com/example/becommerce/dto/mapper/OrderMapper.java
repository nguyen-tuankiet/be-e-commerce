package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.order.OrderPartySummary;
import com.example.becommerce.dto.response.order.OrderPriceAdjustmentResponse;
import com.example.becommerce.dto.response.order.OrderResponse;
import com.example.becommerce.dto.response.order.OrderStatusChangeResponse;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.OrderImage;
import com.example.becommerce.entity.OrderPriceAdjustment;
import com.example.becommerce.entity.OrderPriceAdjustmentPart;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Translates Order entities to API-facing DTOs.
 * Handles party summaries and embedded price adjustments.
 */
@Component
public class OrderMapper {

    /**
     * Full order detail used for GET /orders/:id.
     * Includes the latest price adjustment and all images.
     */
    public OrderResponse toDetailResponse(Order order) {
        if (order == null) return null;

        OrderPriceAdjustment latest = order.getPriceAdjustments().stream()
                .max(Comparator.comparing(OrderPriceAdjustment::getRequestedAt))
                .orElse(null);

        List<String> imageUrls = order.getImages().stream()
                .map(OrderImage::getUrl)
                .toList();

        return OrderResponse.builder()
                .id(order.getCode())
                .status(toApi(order.getStatus()))
                .serviceName(order.getServiceName())
                .subService(order.getSubService())
                .serviceCategory(order.getServiceCategory())
                .deviceName(order.getDeviceName())
                .description(order.getDescription())
                .address(order.getAddress())
                .scheduledAt(order.getScheduledAt())
                .expectedTime(order.getExpectedTime())
                .startedAt(order.getStartedAt())
                .completedAt(order.getCompletedAt())
                .estimatedPrice(order.getEstimatedPrice())
                .finalPrice(order.getFinalPrice())
                .paymentMethod(order.getPaymentMethod() == null ? null : order.getPaymentMethod().apiValue())
                .warrantyMonths(order.getWarrantyMonths())
                .customer(toParty(order.getCustomer(), false))
                .technician(toParty(order.getTechnician(), true))
                .priceAdjustment(toAdjustmentResponse(latest))
                .images(imageUrls.isEmpty() ? null : imageUrls)
                .cancelledBy(order.getCancelledBy() == null ? null : order.getCancelledBy().apiValue())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Lighter shape for GET /orders list view.
     * Drops description/status history/heavy images, keeps key parties + scheduled info.
     */
    public OrderResponse toListItem(Order order) {
        if (order == null) return null;

        return OrderResponse.builder()
                .id(order.getCode())
                .status(toApi(order.getStatus()))
                .serviceName(order.getServiceName())
                .subService(order.getSubService())
                .deviceName(order.getDeviceName())
                .address(order.getAddress())
                .scheduledAt(order.getScheduledAt())
                .estimatedPrice(order.getEstimatedPrice())
                .finalPrice(order.getFinalPrice())
                .customer(toParty(order.getCustomer(), false))
                .technician(toParty(order.getTechnician(), false))
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderStatusChangeResponse toStatusChange(Order order) {
        return OrderStatusChangeResponse.builder()
                .id(order.getCode())
                .status(toApi(order.getStatus()))
                .startedAt(order.getStartedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelledBy(order.getCancelledBy() == null ? null : order.getCancelledBy().apiValue())
                .cancelReason(order.getCancelReason())
                .finalPrice(order.getFinalPrice())
                .technician(toParty(order.getTechnician(), false))
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderPriceAdjustmentResponse toAdjustmentResponse(OrderPriceAdjustment adj) {
        if (adj == null) return null;

        List<OrderPriceAdjustmentResponse.Part> parts = adj.getParts().stream()
                .map(this::toPartResponse)
                .toList();

        return OrderPriceAdjustmentResponse.builder()
                .originalPrice(adj.getOriginalPrice())
                .newPrice(adj.getNewPrice())
                .reason(adj.getReason())
                .status(adj.getStatus() == null ? null : adj.getStatus().apiValue())
                .parts(parts.isEmpty() ? null : parts)
                .requestedAt(adj.getRequestedAt())
                .approvedAt(adj.getApprovedAt())
                .rejectedAt(adj.getRejectedAt())
                .build();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private OrderPartySummary toParty(User user, boolean includeRating) {
        if (user == null) return null;
        return OrderPartySummary.builder()
                .id(user.getCode())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                // Rating left null here — populated by Technician module when wired.
                .rating(null)
                .build();
    }

    private OrderPriceAdjustmentResponse.Part toPartResponse(OrderPriceAdjustmentPart part) {
        return OrderPriceAdjustmentResponse.Part.builder()
                .name(part.getName())
                .price(part.getPrice())
                .partCode(part.getPartCode())
                .build();
    }

    private String toApi(OrderStatus status) {
        return status == null ? null : status.apiValue();
    }
}
