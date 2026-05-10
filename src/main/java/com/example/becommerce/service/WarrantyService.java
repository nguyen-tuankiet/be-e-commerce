package com.example.becommerce.service;

import com.example.becommerce.dto.request.WarrantyClaimRequest;
import com.example.becommerce.dto.request.WarrantyStatusUpdateRequest;
import com.example.becommerce.dto.response.WarrantyClaimResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho warranty claim operations
 */
public interface WarrantyService {

    /**
     * Create warranty claim (customer)
     */
    WarrantyClaimResponse createWarrantyClaim(Long userId, WarrantyClaimRequest request);

    /**
     * Get warranty claim của order
     */
    WarrantyClaimResponse getWarrantyClaim(Long orderId, Long userId, boolean isAdmin);

    /**
     * Get warranty claims của customer
     */
    Page<WarrantyClaimResponse> getCustomerClaims(Long customerId, Pageable pageable);

    /**
     * Get warranty claims của technician
     */
    Page<WarrantyClaimResponse> getTechnicianClaims(Long technicianId, Pageable pageable);

    /**
     * Update warranty status
     */
    WarrantyClaimResponse updateWarrantyStatus(Long claimId, WarrantyStatusUpdateRequest request, Long userId, boolean isAdmin);

    /**
     * Get warranty claims theo status (admin)
     */
    Page<WarrantyClaimResponse> getClaimsByStatus(String status, Pageable pageable);
}
