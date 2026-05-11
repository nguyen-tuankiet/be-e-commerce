package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.order.OrderPartySummary;
import com.example.becommerce.dto.response.warranty.WarrantyResponse;
import com.example.becommerce.entity.WarrantyClaim;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class WarrantyMapper {

    public WarrantyResponse toResponse(WarrantyClaim claim) {
        if (claim == null) return null;

        List<String> images = claim.getImages();
        Long remainingDays = calculateRemainingDays(claim.getWarrantyExpiresAt());

        return WarrantyResponse.builder()
                .id(claim.getCode())
                .orderId(claim.getOrder() == null ? null : claim.getOrder().getCode())
                .status(claim.getStatus() == null ? null : claim.getStatus().apiValue())
                .description(claim.getDescription())
                .images(images != null && !images.isEmpty() ? images : null)
                .scheduledAt(claim.getScheduledAt())
                .warrantyExpiresAt(claim.getWarrantyExpiresAt())
                .remainingDays(remainingDays)
                .technician(claim.getTechnician() == null ? null : OrderPartySummary.builder()
                        .id(claim.getTechnician().getCode())
                        .fullName(claim.getTechnician().getFullName())
                        .phone(claim.getTechnician().getPhone())
                        .avatar(claim.getTechnician().getAvatar())
                        .build())
                .createdAt(claim.getCreatedAt())
                .build();
    }

    private Long calculateRemainingDays(LocalDateTime expiresAt) {
        if (expiresAt == null) return null;
        long days = Duration.between(LocalDateTime.now(), expiresAt).toDays();
        return Math.max(days, 0);
    }
}
