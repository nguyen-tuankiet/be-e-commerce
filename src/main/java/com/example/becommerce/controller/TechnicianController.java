package com.example.becommerce.controller;

import com.example.becommerce.constant.ApiConstant;
import com.example.becommerce.dto.request.TechnicianAvailabilityRequest;
import com.example.becommerce.dto.request.TechnicianRegisterRequest;
import com.example.becommerce.dto.request.TechnicianUpdateProfileRequest;
import com.example.becommerce.dto.response.*;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Technician management controller.
 * Base path: /api/technicians
 */
@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Slf4j
public class TechnicianController {

    private final TechnicianService technicianService;

    // ================================================================
    // 1. POST /api/technicians/register
    // Đăng ký làm thợ
    // ================================================================

    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TechnicianRegisterResponse>> registerTechnician(
            @Valid @RequestBody TechnicianRegisterRequest request) {

        log.info("Register technician request received");

        Long userId = getCurrentUserId();
        TechnicianRegisterResponse response = technicianService.registerTechnician(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // ================================================================
    // 2. GET /api/technicians
    // Lấy danh sách thợ công khai (ACTIVE + APPROVED)
    // ================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TechnicianListItemResponse>>> getTechnicians(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ratingAverage") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get technicians list - page: {}, size: {}, available: {}", page, size, available);

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDir) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "ratingAverage"));

        Page<TechnicianListItemResponse> result = technicianService.getPublicTechnicians(
                keyword, serviceId, province, district, minRating, available, pageable);

        PagedResponse<TechnicianListItemResponse> response = PagedResponse.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ================================================================
    // 3. GET /api/technicians/{id}
    // Chi tiết hồ sơ thợ
    // ================================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianDetailResponse>> getTechnicianDetail(
            @PathVariable Long id) {

        log.info("Get technician detail - id: {}", id);

        Long currentUserId = getOptionalCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        TechnicianDetailResponse response = technicianService.getTechnicianDetail(id, currentUserId, isAdmin);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ================================================================
    // 4. PATCH /api/technicians/{id}/profile
    // Cập nhật hồ sơ thợ (chỉ owner)
    // ================================================================

    @PatchMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TechnicianDetailResponse>> updateTechnicianProfile(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianUpdateProfileRequest request) {

        log.info("Update technician profile - id: {}", id);

        Long currentUserId = getCurrentUserId();
        TechnicianDetailResponse response = technicianService.updateTechnicianProfile(id, request, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ================================================================
    // 5. PATCH /api/technicians/{id}/availability
    // Bật/tắt trạng thái sẵn sàng nhận đơn
    // ================================================================

    @PatchMapping("/{id}/availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TechnicianAvailabilityResponse>> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianAvailabilityRequest request) {

        log.info("Update technician availability - id: {}", id);

        Long currentUserId = getCurrentUserId();
        TechnicianAvailabilityResponse response = technicianService.updateAvailability(id, request, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ================================================================
    // 6. GET /api/technicians/{id}/reviews
    // Lấy danh sách review của thợ
    // ================================================================

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<TechnicianReviewResponse>>> getTechnicianReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Get technician reviews - id: {}, page: {}, size: {}", id, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TechnicianReviewResponse> result = technicianService.getTechnicianReviews(id, pageable);

        PagedResponse<TechnicianReviewResponse> response = PagedResponse.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ================================================================
    // 7. GET /api/technicians/{id}/schedule
    // Lấy lịch làm việc của thợ
    // ================================================================

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<Object>> getTechnicianSchedule(
            @PathVariable Long id,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        log.info("Get technician schedule - id: {}, fromDate: {}, toDate: {}", id, fromDate, toDate);

        Long currentUserId = getOptionalCurrentUserId();
        boolean isOwner = false;

        // Check if current user is owner
        if (currentUserId != null) {
            // TODO: Verify user ID matches technician user ID
        }

        // TODO: Implement schedule fetch logic
        // For now, return placeholder

        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "technicianId", id,
                "message", "Schedule endpoint - TODO: Implement full schedule logic"
        )));
    }

    // ================================================================
    // HELPER METHODS
    // ================================================================

    /**
     * Get current user ID from security context.
     * Throws exception if not authenticated.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("UNAUTHORIZED", "User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser().getId();
        }

        throw new AppException("UNAUTHORIZED", "Cannot extract user ID from authentication");
    }

    /**
     * Get current user ID from security context.
     * Returns null if not authenticated.
     */
    private Long getOptionalCurrentUserId() {
        try {
            return getCurrentUserId();
        } catch (AppException e) {
            return null;
        }
    }

    /**
     * Check if current user is admin.
     * Returns false if not authenticated or not admin.
     */
    private boolean isCurrentUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                String role = ((CustomUserDetails) principal).getUser().getRole().name();
                return "ADMIN".equals(role);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
