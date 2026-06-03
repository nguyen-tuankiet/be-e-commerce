package com.example.becommerce.controller;
import com.example.becommerce.dto.request.BankAccountCreateRequest;
import com.example.becommerce.dto.request.WalletTopUpConfirmRequest;
import com.example.becommerce.dto.request.WalletTopUpRequest;
import com.example.becommerce.dto.request.WalletWithdrawRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.BankAccountDeleteResponse;
import com.example.becommerce.dto.response.BankAccountListResponse;
import com.example.becommerce.dto.response.BankAccountResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.WalletResponse;
import com.example.becommerce.dto.response.WalletTopUpConfirmResponse;
import com.example.becommerce.dto.response.WalletTopUpResponse;
import com.example.becommerce.dto.response.WalletTransactionResponse;
import com.example.becommerce.dto.response.WalletWithdrawResponse;
import com.example.becommerce.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Wallet controller.
 * All endpoints require authenticated users and operate on the current user only.
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class WalletController {

    private static final String TRANSACTIONS_PATH = "/transactions";
    private static final String TOPUP_PATH = "/topup";
    private static final String TOPUP_CONFIRM_PATH = "/topup/confirm";
    private static final String WITHDRAW_PATH = "/withdraw";
    private static final String BANK_ACCOUNTS_PATH = "/bank-accounts";

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getCurrentWallet()));
    }

    @GetMapping(TRANSACTIONS_PATH)
    public ResponseEntity<ApiResponse<PagedResponse<WalletTransactionResponse>>> getTransactions(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        return ResponseEntity.ok(ApiResponse.success(walletService.getTransactions(type, page, limit)));
    }

    @PostMapping(TOPUP_PATH)
    public ResponseEntity<ApiResponse<WalletTopUpResponse>> topUp(@Valid @RequestBody WalletTopUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(walletService.topUp(request)));
    }

    @PostMapping(TOPUP_CONFIRM_PATH)
    public ResponseEntity<ApiResponse<WalletTopUpConfirmResponse>> confirmTopUp(
            @Valid @RequestBody WalletTopUpConfirmRequest request) {
        return ResponseEntity.ok(ApiResponse.success(walletService.confirmTopUp(request)));
    }

    @PostMapping(WITHDRAW_PATH)
    public ResponseEntity<ApiResponse<WalletWithdrawResponse>> withdraw(@Valid @RequestBody WalletWithdrawRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(walletService.withdraw(request)));
    }

    @GetMapping(BANK_ACCOUNTS_PATH)
    public ResponseEntity<ApiResponse<BankAccountListResponse>> getBankAccounts() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getBankAccounts()));
    }

    @PostMapping(BANK_ACCOUNTS_PATH)
    public ResponseEntity<ApiResponse<BankAccountResponse>> createBankAccount(
            @Valid @RequestBody BankAccountCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(walletService.createBankAccount(request)));
    }

    @DeleteMapping(BANK_ACCOUNTS_PATH + "/{id}")
    public ResponseEntity<ApiResponse<BankAccountDeleteResponse>> deleteBankAccount(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(walletService.deleteBankAccount(id)));
    }
}




