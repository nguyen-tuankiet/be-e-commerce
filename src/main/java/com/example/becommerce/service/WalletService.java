package com.example.becommerce.service;

import com.example.becommerce.dto.request.BankAccountCreateRequest;
import com.example.becommerce.dto.request.WalletTopUpConfirmRequest;
import com.example.becommerce.dto.request.WalletTopUpRequest;
import com.example.becommerce.dto.request.WalletWithdrawRequest;
import com.example.becommerce.dto.response.BankAccountDeleteResponse;
import com.example.becommerce.dto.response.BankAccountListResponse;
import com.example.becommerce.dto.response.BankAccountResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.VnpayIpnResponse;
import com.example.becommerce.dto.response.WalletResponse;
import com.example.becommerce.dto.response.WalletTopUpConfirmResponse;
import com.example.becommerce.dto.response.WalletTopUpResponse;
import com.example.becommerce.dto.response.WalletTransactionResponse;
import com.example.becommerce.dto.response.WalletWithdrawResponse;

import java.util.Map;

/**
 * Wallet service contract.
 */
public interface WalletService {

    WalletResponse getCurrentWallet();

    PagedResponse<WalletTransactionResponse> getTransactions(String type, int page, int limit);

    WalletTopUpResponse topUp(WalletTopUpRequest request);

    WalletTopUpConfirmResponse confirmTopUp(WalletTopUpConfirmRequest request);

    WalletWithdrawResponse withdraw(WalletWithdrawRequest request);

    BankAccountListResponse getBankAccounts();

    BankAccountResponse createBankAccount(BankAccountCreateRequest request);

    BankAccountDeleteResponse deleteBankAccount(String id);

    VnpayIpnResponse handleVnpayIpn(Map<String, String> queryParams);

    void createWalletForUser(com.example.becommerce.entity.User user);
}





