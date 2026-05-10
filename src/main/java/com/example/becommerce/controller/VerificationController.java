package com.example.becommerce.controller;

import com.example.becommerce.dto.request.UploadVerificationDocumentRequest;
import com.example.becommerce.dto.request.VerificationSubmitRequest;
import com.example.becommerce.dto.request.VerificationUpdateStatusRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.VerificationResponse;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho KYC verification operations
 * Base path: /api/verifications
 */
@RestController
@RequestMapping("/api/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    /**
     * POST /api/verifications - Submit KYC verification
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerificationResponse>> submitVerification(
            @Valid @RequestBody VerificationSubmitRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        VerificationResponse response = verificationService.submitVerification(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * GET /api/verifications - List verifications (Admin)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<VerificationResponse>>> getAllVerifications(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VerificationResponse> result = verificationService.getVerificationsByStatus(status, pageable);
        PagedResponse<VerificationResponse> response = PagedResponse.of(
                result.getContent(), page, size, result.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/verifications/:id - Get verification detail
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerificationResponse>> getVerification(
            @PathVariable Long id,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        VerificationResponse response = verificationService.getVerification(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PATCH /api/verifications/:id - Admin approve/reject
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VerificationResponse>> updateVerificationStatus(
            @PathVariable Long id,
            @Valid @RequestBody VerificationUpdateStatusRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getUser().getId();
        VerificationResponse response = verificationService.updateVerificationStatus(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/verifications/technician/:technicianId - Query by technician
     */
    @GetMapping("/technician/{technicianId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerificationResponse>> getTechnicianVerification(
            @PathVariable Long technicianId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        VerificationResponse response = verificationService.getTechnicianVerification(technicianId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/verifications/:id/documents - Upload KYC document
     */
    @PostMapping("/{id}/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerificationResponse>> uploadDocument(
            @PathVariable Long id,
            @Valid @RequestBody UploadVerificationDocumentRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        VerificationResponse response = verificationService.uploadDocument(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
