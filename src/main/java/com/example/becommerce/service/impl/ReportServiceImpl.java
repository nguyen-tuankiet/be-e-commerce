package com.example.becommerce.service.impl;

import com.example.becommerce.dto.request.OrderReportRequest;
import com.example.becommerce.dto.request.ReportStatusUpdateRequest;
import com.example.becommerce.dto.response.OrderReportResponse;
import com.example.becommerce.entity.OrderReport;
import com.example.becommerce.entity.OrderReportEvidence;
import com.example.becommerce.entity.enums.ReportStatus;
import com.example.becommerce.exception.ResourceNotFoundException;
import com.example.becommerce.exception.ValidationException;
import com.example.becommerce.repository.OrderReportEvidenceRepository;\nimport com.example.becommerce.repository.OrderReportRepository;
import com.example.becommerce.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderReportRepository reportRepository;
    private final OrderReportEvidenceRepository evidenceRepository;

    @Override
    @Transactional
    public OrderReportResponse createReport(Long userId, OrderReportRequest request) {
        // Check if already has report for this order by this user
        reportRepository.findByOrderId(request.getOrderId())
                .ifPresent(r -> {
                    if (r.getReporterId().equals(userId)) {
                        throw new ValidationException("Report already filed for this order");
                    }
                });

        // Cannot report yourself
        if (request.getAgainstId().equals(userId)) {
            throw new ValidationException("Cannot report yourself");
        }

        // Create report
        OrderReport report = OrderReport.builder()
                .code(generateCode())
                .orderId(request.getOrderId())
                .reporterId(userId)
                .againstId(request.getAgainstId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.OPEN)
                .build();

        OrderReport saved = reportRepository.save(report);
        return mapToResponse(saved);
    }

    @Override
    public OrderReportResponse getReport(Long reportId, Long userId, boolean isAdmin) {
        OrderReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        // Authorization
        if (!isAdmin && !report.getReporterId().equals(userId) && !report.getAgainstId().equals(userId)) {
            throw new ValidationException("Unauthorized");
        }

        return mapToResponse(report);
    }

    @Override
    public Page<OrderReportResponse> getUserReports(Long userId, String type, Pageable pageable) {
        if ("filed".equalsIgnoreCase(type)) {
            return reportRepository.findByReporterId(userId, pageable)
                    .map(this::mapToResponse);
        } else if ("against".equalsIgnoreCase(type)) {
            return reportRepository.findByReporterId(userId, pageable)
                    .map(this::mapToResponse);
        }
        throw new ValidationException("Invalid type");
    }

    @Override
    public Page<OrderReportResponse> getAllReports(String status, Pageable pageable) {
        ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
        return reportRepository.findByStatus(reportStatus, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public OrderReportResponse updateReportStatus(Long reportId, ReportStatusUpdateRequest request, Long adminId) {
        OrderReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        report.setStatus(request.getStatus());
        report.setResolutionNote(request.getResolutionNote());
        report.setResolvedBy(adminId);
        report.setResolvedAt(LocalDateTime.now());

        OrderReport updated = reportRepository.save(report);
        return mapToResponse(updated);
    }

    // Helper
    private String generateCode() {
        return "RPT-" + System.currentTimeMillis();
    }

    private OrderReportResponse mapToResponse(OrderReport r) {
        var evidence = evidenceRepository.findByReportId(r.getReportId())
                .stream()
                .map(OrderReportEvidence::getUrl)
                .collect(Collectors.toList());

        return OrderReportResponse.builder()
                .reportId(r.getReportId())
                .code(r.getCode())
                .orderId(r.getOrderId())
                .reporterId(r.getReporterId())
                .againstId(r.getAgainstId())
                .reason(r.getReason())
                .description(r.getDescription())
                .status(r.getStatus())
                .resolutionNote(r.getResolutionNote())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .resolvedBy(r.getResolvedBy())
                .evidenceUrls(evidence)
                .build();
    }
}
