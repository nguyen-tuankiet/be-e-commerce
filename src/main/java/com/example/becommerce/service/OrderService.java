package com.example.becommerce.service;

import com.example.becommerce.dto.request.OrderCancelRequest;
import com.example.becommerce.dto.request.OrderCreateRequest;
import com.example.becommerce.dto.request.OrderSearchRequest;
import com.example.becommerce.dto.response.OrderResponse;
import com.example.becommerce.dto.response.PagedResponse;

import java.math.BigDecimal;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getOrderById(Long id);

    PagedResponse<OrderResponse> getOrders(OrderSearchRequest request);

    OrderResponse acceptOrder(Long orderId);

    OrderResponse startOrder(Long orderId);

    OrderResponse completeOrder(Long orderId);

    OrderResponse cancelOrder(Long orderId, String cancelReason);

    OrderResponse rejectOrder(Long orderId);

    OrderResponse reportExtraCost(Long orderId, BigDecimal extraCost, String reason);

    OrderResponse approveExtraCost(Long orderId);

    OrderResponse rejectExtraCost(Long orderId);
}
