package com.example.becommerce.service;

import com.example.becommerce.dto.request.report.CreateReportRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.report.ReportResponse;

public interface ReportService {

    ReportResponse createReport(String orderCode, CreateReportRequest request);

    PagedResponse<ReportResponse> listReports(String status, String keyword, int page, int limit);
}
