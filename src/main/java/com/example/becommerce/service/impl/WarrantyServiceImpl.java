package com.example.becommerce.service.impl;

import com.example.becommerce.dto.request.WarrantyClaimRequest;
import com.example.becommerce.dto.request.WarrantyStatusUpdateRequest;
import com.example.becommerce.dto.response.WarrantyClaimResponse;
import com.example.becommerce.entity.WarrantyClaim;
import com.example.becommerce.entity.WarrantyClaimImage;
import com.example.becommerce.entity.enums.WarrantyStatus;
import com.example.becommerce.exception.ResourceNotFoundException;
import com.example.becommerce.exception.ValidationException;
import com.example.becommerce.repository.WarrantyClaimImageRepository;
import com.example.becommerce.repository.WarrantyClaimRepository;
import com.example.becommerce.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyClaimRepository warrantyRepository;
    private final WarrantyClaimImageRepository imageRepository;

    @Override
    @Transactional
    public WarrantyClaimResponse createWarrantyClaim(Long userId, WarrantyClaimRequest request) {
        // Check if warranty already exists for order
        if (warrantyRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new ValidationException("Warranty claim already exists for this order");
        }

        // Create claim
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(request.getWarrantyDays());

        WarrantyClaim claim = WarrantyClaim.builder()
                .code(generateCode())
                .orderId(request.getOrderId())
                .customerId(userId)
                .description(request.getDescription())
                .status(WarrantyStatus.PENDING)
                .warrantyExpiresAt(expiresAt)
                .build();

        WarrantyClaim saved = warrantyRepository.save(claim);
        return mapToResponse(saved);
    }

    @Override
    public WarrantyClaimResponse getWarrantyClaim(Long orderId, Long userId, boolean isAdmin) {
        WarrantyClaim claim = warrantyRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found"));

        // Authorization
        if (!isAdmin && !claim.getCustomerId().equals(userId) && 
            (claim.getTechnicianId() == null || !claim.getTechnicianId().equals(userId))) {
            throw new ValidationException("Unauthorized");
        }

        return mapToResponse(claim);
    }

    @Override
    public Page<WarrantyClaimResponse> getCustomerClaims(Long customerId, Pageable pageable) {
        return warrantyRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<WarrantyClaimResponse> getTechnicianClaims(Long technicianId, Pageable pageable) {
        return warrantyRepository.findByTechnicianId(technicianId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public WarrantyClaimResponse updateWarrantyStatus(Long claimId, WarrantyStatusUpdateRequest request, Long userId, boolean isAdmin) {
        WarrantyClaim claim = warrantyRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found"));

        // Authorization: technician or admin
        if (!isAdmin && (claim.getTechnicianId() == null || !claim.getTechnicianId().equals(userId))) {
            throw new ValidationException("Unauthorized");
        }

        claim.setStatus(request.getStatus());
        if (request.getStatus() == WarrantyStatus.COMPLETED || 
            request.getStatus() == WarrantyStatus.REJECTED) {
            claim.setResolvedAt(LocalDateTime.now());
        }

        WarrantyClaim updated = warrantyRepository.save(claim);
        return mapToResponse(updated);
    }

    @Override
    public Page<WarrantyClaimResponse> getClaimsByStatus(String status, Pageable pageable) {
        WarrantyStatus warStatus = WarrantyStatus.valueOf(status.toUpperCase());
        return warrantyRepository.findByStatus(warStatus, pageable)
                .map(this::mapToResponse);
    }

    // Helper
    private String generateCode() {
        return "WAR-" + System.currentTimeMillis();
    }

    private WarrantyClaimResponse mapToResponse(WarrantyClaim claim) {
        var images = imageRepository.findByWarrantyClaimId(claim.getClaimId())
                .stream()
                .map(WarrantyClaimImage::getUrl)
                .collect(Collectors.toList());

        return WarrantyClaimResponse.builder()
                .claimId(claim.getClaimId())
                .code(claim.getCode())
                .orderId(claim.getOrderId())
                .customerId(claim.getCustomerId())
                .technicianId(claim.getTechnicianId())
                .description(claim.getDescription())
                .status(claim.getStatus())
                .warrantyExpiresAt(claim.getWarrantyExpiresAt())
                .scheduledAt(claim.getScheduledAt())
                .resolvedAt(claim.getResolvedAt())
                .createdAt(claim.getCreatedAt())
                .imageUrls(images)
                .build();
    }
}
