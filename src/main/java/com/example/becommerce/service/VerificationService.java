package com.example.becommerce.service;

import com.example.becommerce.dto.request.UploadVerificationDocumentRequest;
import com.example.becommerce.dto.request.VerificationSubmitRequest;
import com.example.becommerce.dto.request.VerificationUpdateStatusRequest;
import com.example.becommerce.dto.response.VerificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho KYC verification operations
 */
public interface VerificationService {

    /**
     * Submit KYC verification (technician)
     */
    VerificationResponse submitVerification(Long userId, VerificationSubmitRequest request);

    /**
     * Get verification detail (owner, admin)
     */
    VerificationResponse getVerification(Long verificationId, Long userId, boolean isAdmin);

    /**
     * Get verification của technician (owner, admin)
     */
    VerificationResponse getTechnicianVerification(Long technicianId, Long userId, boolean isAdmin);

    /**
     * Get tất cả verifications theo status (admin)
     */
    Page<VerificationResponse> getVerificationsByStatus(String status, Pageable pageable);

    /**
     * Approve/reject verification (admin)
     */
    VerificationResponse updateVerificationStatus(Long verificationId, VerificationUpdateStatusRequest request, Long adminId);

    /**
     * Upload tài liệu KYC
     */
    VerificationResponse uploadDocument(Long verificationId, UploadVerificationDocumentRequest request, Long userId);

    /**
     * Check if technician đã KYC approved
     */
    boolean isKycApproved(Long technicianId);
}
