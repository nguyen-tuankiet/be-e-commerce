package com.example.becommerce.controller;

import com.example.becommerce.dto.request.WarrantyClaimRequest;
import com.example.becommerce.dto.request.WarrantyStatusUpdateRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.WarrantyClaimResponse;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.WarrantyService;
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
 * Controller cho warranty claim operations
 * Base path: /api/warranty
 */
@RestController
@RequestMapping("/api/warranty")
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    /**
     * POST /api/warranty - Create warranty claim
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WarrantyClaimResponse>> createWarrantyClaim(
            @Valid @RequestBody WarrantyClaimRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        WarrantyClaimResponse response = warrantyService.createWarrantyClaim(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * GET /api/warranty/orders/:orderId - Get warranty for order
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WarrantyClaimResponse>> getWarrantyClaim(
            @PathVariable Long orderId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        WarrantyClaimResponse response = warrantyService.getWarrantyClaim(orderId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PATCH /api/warranty/:id/status - Update warranty status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WarrantyClaimResponse>> updateWarrantyStatus(
            @PathVariable Long id,
            @Valid @RequestBody WarrantyStatusUpdateRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        WarrantyClaimResponse response = warrantyService.updateWarrantyStatus(id, request, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
