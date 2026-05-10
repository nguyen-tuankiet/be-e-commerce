package com.example.becommerce.controller;

import com.example.becommerce.dto.request.OrderReportRequest;
import com.example.becommerce.dto.request.ReportStatusUpdateRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.OrderReportResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.ReportService;
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
 * Controller cho order report/complaint operations
 * Base path: /api/reports
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * POST /api/reports - Create order report
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderReportResponse>> createReport(
            @Valid @RequestBody OrderReportRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        OrderReportResponse response = reportService.createReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * GET /api/reports - List reports (Admin)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<OrderReportResponse>>> getAllReports(
            @RequestParam(defaultValue = "OPEN") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderReportResponse> result = reportService.getAllReports(status, pageable);
        PagedResponse<OrderReportResponse> response = PagedResponse.of(
                result.getContent(), page, size, result.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PATCH /api/reports/:id/status - Update report status (Admin)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderReportResponse>> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReportStatusUpdateRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long adminId = userDetails.getUser().getId();
        OrderReportResponse response = reportService.updateReportStatus(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
