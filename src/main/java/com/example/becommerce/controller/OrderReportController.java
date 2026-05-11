package com.example.becommerce.controller;

import com.example.becommerce.dto.request.report.CreateReportRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.report.ReportResponse;
import com.example.becommerce.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/orders/:id/reports — file an incident report on an order.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderReportController {

    private final ReportService reportService;

    @PostMapping("/{id}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @PathVariable("id") String orderCode,
            @Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reportService.createReport(orderCode, request)));
    }
}
