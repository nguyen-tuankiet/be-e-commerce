package com.example.becommerce.controller;

import com.example.becommerce.dto.request.order.CancelOrderRequest;
import com.example.becommerce.dto.request.order.CompleteOrderRequest;
import com.example.becommerce.dto.request.order.CreateOrderRequest;
import com.example.becommerce.dto.request.order.PriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.RejectOrderRequest;
import com.example.becommerce.dto.request.order.RejectPriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.UpdateOrderStatusRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.order.OrderResponse;
import com.example.becommerce.dto.response.order.OrderStatusChangeResponse;
import com.example.becommerce.dto.response.order.PriceAdjustmentEnvelope;
import com.example.becommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for the Order module — covers customer creation, technician
 * acceptance/rejection/completion, status updates and price adjustments.
 *
 * <p>Authorization is enforced inside {@code OrderService} per the role of
 * the current principal because rules vary per action.</p>
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    // ---- Listing / detail ------------------------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrders(status, keyword, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable("id") String code) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(code)));
    }

    // ---- Create (customer) -----------------------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.createOrder(request)));
    }

    // ---- Status transitions ----------------------------------------

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderStatusChangeResponse>> updateStatus(
            @PathVariable("id") String code,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(code, request)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderStatusChangeResponse>> cancelOrder(
            @PathVariable("id") String code,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(code, request)));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<OrderStatusChangeResponse>> acceptOrder(@PathVariable("id") String code) {
        return ResponseEntity.ok(ApiResponse.success(orderService.acceptOrder(code)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<OrderStatusChangeResponse>> rejectOrder(
            @PathVariable("id") String code,
            @Valid @RequestBody RejectOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.rejectOrder(code, request)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderStatusChangeResponse>> completeOrder(
            @PathVariable("id") String code,
            @Valid @RequestBody CompleteOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.completeOrder(code, request)));
    }

    // ---- Price adjustment ------------------------------------------

    @PatchMapping("/{id}/price")
    public ResponseEntity<ApiResponse<PriceAdjustmentEnvelope>> requestPriceAdjustment(
            @PathVariable("id") String code,
            @Valid @RequestBody PriceAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.requestPriceAdjustment(code, request)));
    }

    @PostMapping("/{id}/price/approve")
    public ResponseEntity<ApiResponse<PriceAdjustmentEnvelope>> approvePriceAdjustment(
            @PathVariable("id") String code) {
        return ResponseEntity.ok(ApiResponse.success(orderService.approvePriceAdjustment(code)));
    }

    @PostMapping("/{id}/price/reject")
    public ResponseEntity<ApiResponse<PriceAdjustmentEnvelope>> rejectPriceAdjustment(
            @PathVariable("id") String code,
            @Valid @RequestBody RejectPriceAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.rejectPriceAdjustment(code, request)));
    }
}
