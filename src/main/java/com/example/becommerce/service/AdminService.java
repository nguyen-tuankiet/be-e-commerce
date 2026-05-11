package com.example.becommerce.service;

import com.example.becommerce.dto.request.admin.AdminSettingsRequest;
import com.example.becommerce.dto.request.admin.CommissionUpdateRequest;
import com.example.becommerce.dto.request.admin.WalletAdjustRequest;
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

public interface AdminService {
    AdminStatsResponse getStats();

    RevenueStatsResponse getRevenueStats(String range);

    ServiceDistributionResponse getServiceDistribution();

    RecentOrdersResponse getRecentOrders(int limit);

    AdminTransactionsResponse getTransactions(String type, String date, int page, int limit);

    WithdrawRequestsResponse getWithdrawRequests();

    WithdrawApproveResponse approveWithdrawRequest(String id);

    CommissionResponse updateCommission(CommissionUpdateRequest request);

    WalletAdjustResponse adjustWallet(WalletAdjustRequest request);

    AdminSettingsRequest getSettings();

    AdminSettingsSavedResponse updateSettings(AdminSettingsRequest request);
}
