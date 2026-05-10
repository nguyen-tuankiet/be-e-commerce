package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.WarrantyStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho warranty claim
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyClaimResponse {
    private Long claimId;
    private String code;
    private Long orderId;
    private Long customerId;
    private Long technicianId;
    private String description;
    private WarrantyStatus status;
    private LocalDateTime warrantyExpiresAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
}
