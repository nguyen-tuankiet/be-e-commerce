package com.example.becommerce.controller;

import com.example.becommerce.constant.ApiConstant;
import com.example.becommerce.dto.request.ExtraCostRequest;
import com.example.becommerce.dto.request.OrderCancelRequest;
import com.example.becommerce.dto.request.OrderCreateRequest;
import com.example.becommerce.dto.request.OrderSearchRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.OrderResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstant.ORDER_BASE)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        OrderResponse data = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse data = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getOrders(
            @Valid @ModelAttribute OrderSearchRequest request) {
        PagedResponse<OrderResponse> data = orderService.getOrders(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<OrderResponse>> acceptOrder(@PathVariable Long id) {
        OrderResponse data = orderService.acceptOrder(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<OrderResponse>> startOrder(@PathVariable Long id) {
        OrderResponse data = orderService.startOrder(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(@PathVariable Long id) {
        OrderResponse data = orderService.completeOrder(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderCancelRequest request) {
        OrderResponse data = orderService.cancelOrder(id, request.getCancelReason());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectOrder(@PathVariable Long id) {
        OrderResponse data = orderService.rejectOrder(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/{id}/extra-costs")
    public ResponseEntity<ApiResponse<OrderResponse>> reportExtraCost(
            @PathVariable Long id,
            @Valid @RequestBody ExtraCostRequest request) {
        OrderResponse data = orderService.reportExtraCost(id, request.getExtraCost(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/{id}/extra-costs/approve")
    public ResponseEntity<ApiResponse<OrderResponse>> approveExtraCost(@PathVariable Long id) {
        OrderResponse data = orderService.approveExtraCost(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/{id}/extra-costs/reject")
    public ResponseEntity<ApiResponse<OrderResponse>> rejectExtraCost(@PathVariable Long id) {
        OrderResponse data = orderService.rejectExtraCost(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
