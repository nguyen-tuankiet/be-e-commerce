package com.example.becommerce.service;

import com.example.becommerce.dto.request.OrderReportRequest;
import com.example.becommerce.dto.request.ReportStatusUpdateRequest;
import com.example.becommerce.dto.response.OrderReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho order report/complaint operations
 */
public interface ReportService {

    /**
     * Create report cho order
     */
    OrderReportResponse createReport(Long userId, OrderReportRequest request);

    /**
     * Get report detail (owner, admin)
     */
    OrderReportResponse getReport(Long reportId, Long userId, boolean isAdmin);

    /**
     * Get reports của user
     */
    Page<OrderReportResponse> getUserReports(Long userId, String type, Pageable pageable);

    /**
     * Get tất cả reports (admin)
     */
    Page<OrderReportResponse> getAllReports(String status, Pageable pageable);

    /**
     * Update report status (admin)
     */
    OrderReportResponse updateReportStatus(Long reportId, ReportStatusUpdateRequest request, Long adminId);
}
