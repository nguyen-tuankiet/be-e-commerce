package com.example.becommerce.service;

import com.example.becommerce.dto.request.admin.AdminSettingsRequest;
import com.example.becommerce.dto.request.admin.CommissionUpdateRequest;
import com.example.becommerce.dto.request.admin.WalletAdjustRequest;
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

public interface AdminService {
    AdminStatsResponse getStats(String mode, Integer year, Integer quarter, Integer month);

    RevenueStatsResponse getRevenueStats(String mode, Integer year, Integer quarter, Integer month);

    ServiceDistributionResponse getServiceDistribution(String mode, Integer year, Integer quarter, Integer month);

    RecentOrdersResponse getRecentOrders(String mode, Integer year, Integer quarter, Integer month, int limit);

    AdminTransactionsResponse getTransactions(String type, String date, int page, int limit);

    WithdrawRequestsResponse getWithdrawRequests();

    WithdrawApproveResponse approveWithdrawRequest(String id);

    CommissionResponse updateCommission(CommissionUpdateRequest request);

    CommissionSettingsResponse getCommissionSettings();

    CommissionWalletsResponse getCommissionWallets(String status, String keyword, int page, int size);

    WalletAdjustResponse adjustWallet(WalletAdjustRequest request);

    AdminSettingsRequest getSettings();

    AdminSettingsSavedResponse updateSettings(AdminSettingsRequest request);
}


