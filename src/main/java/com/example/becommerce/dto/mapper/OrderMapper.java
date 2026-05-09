package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.OrderResponse;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) return null;

        User customer = order.getCustomer();
        User technician = order.getTechnician();

        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .status(order.getStatus().name().toLowerCase())
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .customerPhone(customer.getPhone())
                .technicianId(technician != null ? technician.getId() : null)
                .technicianName(technician != null ? technician.getFullName() : null)
                .technicianPhone(technician != null ? technician.getPhone() : null)
                .serviceName(order.getServiceName())
                .description(order.getDescription())
                .address(order.getAddress())
                .district(order.getDistrict())
                .totalPrice(order.getTotalPrice())
                .note(order.getNote())
                .cancelReason(order.getCancelReason())
                .extraCost(order.getExtraCost())
                .extraCostReason(order.getExtraCostReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
