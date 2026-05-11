package com.example.becommerce.controller;

import com.example.becommerce.dto.request.admin.AdminSettingsRequest;
import com.example.becommerce.dto.request.admin.CommissionUpdateRequest;
import com.example.becommerce.dto.request.admin.WalletAdjustRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.admin.AdminSettingsSavedResponse;
import com.example.becommerce.dto.response.admin.AdminStatsResponse;
import com.example.becommerce.dto.response.admin.AdminTransactionsResponse;
import com.example.becommerce.dto.response.admin.CommissionResponse;
import com.example.becommerce.dto.response.admin.RecentOrdersResponse;
import com.example.becommerce.dto.response.admin.RevenueStatsResponse;
import com.example.becommerce.dto.response.admin.ServiceDistributionResponse;
import com.example.becommerce.dto.response.admin.WalletAdjustResponse;
import com.example.becommerce.dto.response.admin.WithdrawApproveResponse;
import com.example.becommerce.dto.response.admin.WithdrawRequestsResponse;
import com.example.becommerce.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getStats()));
    }

    @GetMapping("/stats/revenue")
    public ResponseEntity<ApiResponse<RevenueStatsResponse>> getRevenueStats(@RequestParam(defaultValue = "7days") String range) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getRevenueStats(range)));
    }

    @GetMapping("/stats/service-distribution")
    public ResponseEntity<ApiResponse<ServiceDistributionResponse>> getServiceDistribution() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getServiceDistribution()));
    }

    @GetMapping("/orders/recent")
    public ResponseEntity<ApiResponse<RecentOrdersResponse>> getRecentOrders(@RequestParam(defaultValue = "5") @Min(1) int limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getRecentOrders(limit)));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<AdminTransactionsResponse>> getTransactions(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getTransactions(type, date, page, limit)));
    }

    @GetMapping("/withdraw-requests")
    public ResponseEntity<ApiResponse<WithdrawRequestsResponse>> getWithdrawRequests() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getWithdrawRequests()));
    }

    @PostMapping("/withdraw-requests/{id}/approve")
    public ResponseEntity<ApiResponse<WithdrawApproveResponse>> approveWithdrawRequest(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveWithdrawRequest(id)));
    }

    @PatchMapping("/commission")
    public ResponseEntity<ApiResponse<CommissionResponse>> updateCommission(@Valid @RequestBody CommissionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateCommission(request)));
    }

    @PostMapping("/wallet/adjust")
    public ResponseEntity<ApiResponse<WalletAdjustResponse>> adjustWallet(@Valid @RequestBody WalletAdjustRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.adjustWallet(request)));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<AdminSettingsRequest>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSettings()));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<AdminSettingsSavedResponse>> updateSettings(@Valid @RequestBody AdminSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateSettings(request)));
    }
}
