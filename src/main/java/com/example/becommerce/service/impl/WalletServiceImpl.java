package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.BankAccountMapper;
import com.example.becommerce.dto.mapper.WalletMapper;
import com.example.becommerce.dto.mapper.WalletTransactionMapper;
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
import com.example.becommerce.entity.BankAccount;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.Wallet;
import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.entity.enums.PaymentMethod;
import com.example.becommerce.entity.enums.TransactionStatus;
import com.example.becommerce.entity.enums.TransactionType;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.BankAccountRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.repository.WalletRepository;
import com.example.becommerce.repository.WalletTransactionRepository;
import com.example.becommerce.service.WalletService;
import com.example.becommerce.service.PaymentGatewayService;
import com.example.becommerce.utils.MoneyUtils;
import com.example.becommerce.utils.BankAccountMaskUtils;
import com.example.becommerce.utils.TransactionCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Wallet service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final BankAccountMapper bankAccountMapper;
    private final TransactionCodeGenerator transactionCodeGenerator;
    private final PaymentGatewayService paymentGatewayService;

    @Value("${app.wallet.default-currency:VND}")
    private String defaultCurrency;

    @Value("${app.wallet.topup-min-amount:10000}")
    private BigDecimal topupMinAmount;

    @Value("${app.wallet.withdraw-min-amount:50000}")
    private BigDecimal withdrawMinAmount;

    @Value("${app.wallet.withdraw-fee:5000}")
    private BigDecimal withdrawFee;

    @Value("${app.wallet.topup-expiry-minutes:30}")
    private int topupExpiryMinutes;

    @Override
    @Transactional
    public WalletResponse getCurrentWallet() {
        User currentUser = getCurrentUser();
        Wallet wallet = getOrCreateWallet(currentUser);
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WalletTransactionResponse> getTransactions(String type, int page, int limit) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, limit), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<WalletTransaction> transactionPage;
        if (!StringUtils.hasText(type) || "all".equalsIgnoreCase(type)) {
            transactionPage = walletTransactionRepository.findByWallet_User_IdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        } else {
            TransactionType transactionType;
            try {
                transactionType = TransactionType.from(type);
            } catch (IllegalArgumentException ex) {
                throw AppException.badRequest(ErrorCode.INVALID_TRANSACTION_TYPE, "Loại giao dịch không hợp lệ");
            }
            transactionPage = walletTransactionRepository.findByWallet_User_IdAndTypeOrderByCreatedAtDesc(currentUser.getId(), transactionType, pageable);
        }

        List<WalletTransactionResponse> items = transactionPage.getContent()
                .stream()
                .map(walletTransactionMapper::toResponse)
                .toList();

        return PagedResponse.of(items, page, limit, transactionPage.getTotalElements());
    }

    @Override
    @Transactional
    public WalletTopUpResponse topUp(WalletTopUpRequest request) {
        User currentUser = getCurrentUser();
        Wallet wallet = getOrCreateWallet(currentUser);

        BigDecimal amount = MoneyUtils.normalize(request.getAmount());
        if (amount.compareTo(topupMinAmount) < 0) {
            throw AppException.badRequest(ErrorCode.TOPUP_AMOUNT_TOO_SMALL, "Số tiền nạp tối thiểu là 10,000");
        }

        PaymentMethod paymentMethod = PaymentMethod.from(request.getMethod());
        if (paymentMethod != PaymentMethod.VNPAY) {
            throw AppException.badRequest(ErrorCode.INVALID_PAYMENT_METHOD, "Hiện tại chỉ hỗ trợ thanh toán bằng VNPay");
        }

        String transactionCode = generateUniqueTransactionCode(TransactionType.TOPUP);

        WalletTransaction transaction = WalletTransaction.builder()
                .transactionCode(transactionCode)
                .wallet(wallet)
                .type(TransactionType.TOPUP)
                .category(MoneyUtils.buildCategory(TransactionType.TOPUP))
                .title("Nạp tiền vào ví")
                .amount(amount)
                .fee(BigDecimal.ZERO)
                .netAmount(amount)
                .status(TransactionStatus.AWAITING_PAYMENT)
                .paymentMethod(paymentMethod)
                .transferContent(null)
                .qrCode(null)
                .expiredAt(LocalDateTime.now().plusMinutes(topupExpiryMinutes))
                .build();

        transaction = walletTransactionRepository.save(transaction);

        PaymentGatewayService.GatewayCheckoutData checkout = paymentGatewayService.createCheckout(transaction, paymentMethod);

        return WalletTopUpResponse.builder()
                .transactionId(transactionCode)
                .amount(amount)
                .method(paymentMethod.apiValue())
                .checkoutUrl(checkout.checkoutUrl())
                .deepLink(checkout.deepLink())
                .qrCodeUrl(checkout.qrCodeUrl())
                .paymentInfo(null)
                .expiredAt(transaction.getExpiredAt())
                .status(transaction.getStatus().apiValue())
                .build();
    }

    @Override
    @Transactional
    public VnpayIpnResponse handleVnpayIpn(Map<String, String> queryParams) {
        return paymentGatewayService.processVnpayIpn(queryParams);
    }

    @Override
    @Transactional
    public WalletTopUpConfirmResponse confirmTopUp(WalletTopUpConfirmRequest request) {
        User currentUser = getCurrentUser();
        WalletTransaction transaction = walletTransactionRepository
                .findByTransactionCodeAndWallet_User_Id(request.getTransactionId(), currentUser.getId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy giao dịch nạp tiền"));

        ensureTopUpTransactionCanBeConfirmed(transaction);

        transaction.setStatus(TransactionStatus.PENDING_VERIFICATION);
        walletTransactionRepository.save(transaction);

        return WalletTopUpConfirmResponse.builder()
                .transactionId(transaction.getTransactionCode())
                .status(transaction.getStatus().apiValue())
                .message("Yêu cầu đang được xác minh")
                .build();
    }

    @Override
    @Transactional
    public WalletWithdrawResponse withdraw(WalletWithdrawRequest request) {
        User currentUser = getCurrentUser();
        Wallet wallet = walletRepository.findWithLockByUser_Id(currentUser.getId())
                .orElseGet(() -> getOrCreateWallet(currentUser));

        BigDecimal amount = MoneyUtils.normalize(request.getAmount());
        if (amount.compareTo(withdrawMinAmount) < 0) {
            throw AppException.badRequest(ErrorCode.WITHDRAW_AMOUNT_TOO_SMALL, "Số tiền rút tối thiểu là 50,000");
        }
        if (amount.compareTo(withdrawFee) <= 0) {
            throw AppException.badRequest(ErrorCode.WITHDRAW_AMOUNT_TOO_SMALL, "Số tiền rút phải lớn hơn phí rút");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw AppException.badRequest(ErrorCode.INSUFFICIENT_BALANCE, "Số dư ví không đủ để thực hiện rút tiền");
        }

        BankAccount bankAccount = bankAccountRepository
                .findByCodeAndUser_Id(request.getBankAccountId(), currentUser.getId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy tài khoản ngân hàng"));

        BigDecimal netAmount = amount.subtract(withdrawFee);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setPendingBalance(wallet.getPendingBalance().add(netAmount));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(amount));
        walletRepository.save(wallet);

        String transactionCode = generateUniqueTransactionCode(TransactionType.WITHDRAW);
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionCode(transactionCode)
                .wallet(wallet)
                .type(TransactionType.WITHDRAW)
                .category(MoneyUtils.buildCategory(TransactionType.WITHDRAW))
                .title("Yêu cầu rút tiền về ngân hàng")
                .amount(amount)
                .fee(withdrawFee)
                .netAmount(netAmount)
                .status(TransactionStatus.PENDING)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .bankAccount(bankAccount)
                .transferContent("WITHDRAW-" + bankAccount.getCode() + " - " + amount.toPlainString())
                .build();
        walletTransactionRepository.save(transaction);

        return WalletWithdrawResponse.builder()
                .transactionId(transactionCode)
                .amount(amount)
                .fee(withdrawFee)
                .netAmount(netAmount)
                .bankAccount(WalletWithdrawResponse.BankAccountInfo.builder()
                        .bankName(bankAccount.getBankName())
                        .accountNumber(BankAccountMaskUtils.mask(bankAccount.getAccountNumber()))
                        .owner(bankAccount.getAccountOwner())
                        .build())
                .status(transaction.getStatus().apiValue())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountListResponse getBankAccounts() {
        User currentUser = getCurrentUser();
        List<BankAccountResponse> items = bankAccountRepository.findByUser_IdOrderByCreatedAtAsc(currentUser.getId())
                .stream()
                .map(bankAccountMapper::toResponse)
                .toList();

        return BankAccountListResponse.builder()
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public BankAccountResponse createBankAccount(BankAccountCreateRequest request) {
        User currentUser = getCurrentUser();

        String accountNumber = request.getAccountNumber().trim();
        if (bankAccountRepository.existsByUser_IdAndAccountNumber(currentUser.getId(), accountNumber)) {
            throw AppException.conflict(ErrorCode.BANK_ACCOUNT_ALREADY_EXISTS, "Tài khoản ngân hàng đã tồn tại");
        }

        boolean isFirstAccount = bankAccountRepository.countByUser_Id(currentUser.getId()) == 0;
        BankAccount bankAccount = BankAccount.builder()
                .code(generateUniqueBankAccountCode())
                .user(currentUser)
                .bankName(request.getBankName().trim())
                .accountNumber(accountNumber)
                .accountOwner(request.getAccountOwner().trim())
                .defaultAccount(isFirstAccount)
                .build();

        try {
            bankAccount = bankAccountRepository.save(bankAccount);
        } catch (DataIntegrityViolationException ex) {
            throw AppException.conflict(ErrorCode.BANK_ACCOUNT_ALREADY_EXISTS, "Tài khoản ngân hàng đã tồn tại");
        }

        return bankAccountMapper.toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountDeleteResponse deleteBankAccount(String id) {
        User currentUser = getCurrentUser();
        BankAccount bankAccount = bankAccountRepository.findByCodeAndUser_Id(id, currentUser.getId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy tài khoản ngân hàng"));

        long accountCount = bankAccountRepository.countByUser_Id(currentUser.getId());
        if (bankAccount.isDefaultAccount() && accountCount > 1) {
            throw AppException.badRequest(ErrorCode.BANK_ACCOUNT_DELETE_NOT_ALLOWED,
                    "Không thể xóa tài khoản ngân hàng mặc định khi còn tài khoản khác");
        }

        bankAccountRepository.delete(bankAccount);
        return BankAccountDeleteResponse.builder()
                .message("Xóa tài khoản ngân hàng thành công")
                .build();
    }

    @Override
    @Transactional
    public void createWalletForUser(User user) {
        if (user == null) {
            return;
        }
        walletRepository.findByUser_Id(user.getId()).ifPresentOrElse(
                wallet -> log.debug("Wallet already exists for user {}", user.getCode()),
                () -> {
                    try {
                        walletRepository.save(Wallet.builder()
                                .user(user)
                                .balance(BigDecimal.ZERO)
                                .pendingBalance(BigDecimal.ZERO)
                                .totalEarned(BigDecimal.ZERO)
                                .totalWithdrawn(BigDecimal.ZERO)
                                .currency(defaultCurrency)
                                .build());
                    } catch (DataIntegrityViolationException ex) {
                        log.debug("Wallet race detected for user {}, reloading existing wallet", user.getCode());
                    }
                }
        );
    }

    private Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    try {
                        return walletRepository.save(Wallet.builder()
                                .user(user)
                                .balance(BigDecimal.ZERO)
                                .pendingBalance(BigDecimal.ZERO)
                                .totalEarned(BigDecimal.ZERO)
                                .totalWithdrawn(BigDecimal.ZERO)
                                .currency(defaultCurrency)
                                .build());
                    } catch (DataIntegrityViolationException ex) {
                        return walletRepository.findByUser_Id(user.getId())
                                .orElseThrow(() -> AppException.notFound("Không thể khởi tạo ví cho người dùng"));
                    }
                });
    }

    private void ensureTopUpTransactionCanBeConfirmed(WalletTransaction transaction) {
        if (transaction.getPaymentMethod() != PaymentMethod.VIETQR) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST,
                    "Giao dịch này cần được xác nhận qua callback từ cổng thanh toán");
        }
        if (transaction.getStatus() != TransactionStatus.AWAITING_PAYMENT) {
            throw AppException.badRequest(ErrorCode.TRANSACTION_ALREADY_VERIFIED,
                    "Giao dịch đã được xác minh hoặc không ở trạng thái chờ thanh toán");
        }
        if (transaction.getExpiredAt() != null && transaction.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw AppException.badRequest(ErrorCode.TRANSACTION_EXPIRED, "Giao dịch đã hết hạn");
        }
    }

    private String generateUniqueTransactionCode(TransactionType type) {
        for (int i = 0; i < 5; i++) {
            String code = transactionCodeGenerator.generateTransactionCode(type);
            if (!walletTransactionRepository.existsByTransactionCode(code)) {
                return code;
            }
        }
        throw AppException.badRequest(ErrorCode.INTERNAL_SERVER_ERROR, "Không thể sinh mã giao dịch");
    }

    private String generateUniqueBankAccountCode() {
        for (int i = 0; i < 5; i++) {
            String code = transactionCodeGenerator.generateBankAccountCode();
            if (!bankAccountRepository.existsByCode(code)) {
                return code;
            }
        }
        return transactionCodeGenerator.generateBankAccountCode();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw AppException.unauthorized("Người dùng chưa đăng nhập");
        }

        String email = authentication.getName();
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng hiện tại"));
    }
}







