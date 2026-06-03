package com.example.becommerce.controller;

import com.example.becommerce.dto.request.admin.AdminSettingsRequest;
import com.example.becommerce.dto.request.admin.CommissionUpdateRequest;
import com.example.becommerce.dto.request.admin.WalletAdjustRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.admin.AdminSettingsSavedResponse;
import com.example.becommerce.dto.response.admin.AdminStatsResponse;
import com.example.becommerce.dto.response.admin.AdminTransactionsResponse;
import com.example.becommerce.dto.response.admin.CommissionResponse;
import com.example.becommerce.dto.response.admin.CommissionSettingsResponse;
import com.example.becommerce.dto.response.admin.CommissionWalletsResponse;
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

/**
 * Admin Management Controller
 *
 * Provides endpoints for:
 * - Dashboard statistics and analytics
 * - Commission configuration (PATCH /api/admin/commission, GET /api/admin/commission-settings)
 * - Wallet management and adjustments
 * - General platform settings
 *
 * Swagger/OpenAPI Documentation:
 * To enable Swagger UI, add the following dependency to pom.xml:
 * <dependency>
 *     <groupId>org.springdoc</groupId>
 *     <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
 *     <version>2.0.2</version>
 * </dependency>
 *
 * Then uncomment the @Operation and @ApiResponse annotations below.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats(
            @RequestParam(defaultValue = "month") String mode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getStats(mode, year, quarter, month)));
    }

    @GetMapping("/dashboard/revenue")
    public ResponseEntity<ApiResponse<RevenueStatsResponse>> getRevenueStats(
            @RequestParam(defaultValue = "month") String mode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getRevenueStats(mode, year, quarter, month)));
    }

    @GetMapping("/stats/service-distribution")
    public ResponseEntity<ApiResponse<ServiceDistributionResponse>> getServiceDistribution(
            @RequestParam(defaultValue = "month") String mode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getServiceDistribution(mode, year, quarter, month)));
    }

    @GetMapping("/dashboard/recent-orders")
    public ResponseEntity<ApiResponse<RecentOrdersResponse>> getRecentOrders(
            @RequestParam(defaultValue = "month") String mode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "5") @Min(1) int limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getRecentOrders(mode, year, quarter, month, limit)));
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
    // @Operation(summary = "Update commission settings", description = "Update fixed commission fee and minimum commission balance")
    // @ApiResponse(responseCode = "200", description = "Commission settings updated successfully")
    // @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    // @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public ResponseEntity<ApiResponse<CommissionResponse>> updateCommission(@Valid @RequestBody CommissionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateCommission(request)));
    }

    /**
     * Retrieve current commission settings.
     *
     * Returns comprehensive commission configuration including fixed fee, minimum balance,
     * and auto-lock status.
     *
     * @return CommissionSettingsResponse with current settings
     */
    @GetMapping("/commission-settings")
    // @Operation(summary = "Get commission settings", description = "Retrieve current commission configuration including fixed fee, minimum balance, and auto-lock status")
    // @ApiResponse(responseCode = "200", description = "Commission settings retrieved successfully")
    // @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public ResponseEntity<ApiResponse<CommissionSettingsResponse>> getCommissionSettings() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCommissionSettings()));
    }

    @GetMapping("/commission-wallets")
    public ResponseEntity<ApiResponse<CommissionWalletsResponse>> getCommissionWallets(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCommissionWallets(status, keyword, page, size)));
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
