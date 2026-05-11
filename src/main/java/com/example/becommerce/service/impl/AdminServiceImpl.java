package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.request.admin.AdminSettingsRequest;
import com.example.becommerce.dto.request.admin.CommissionUpdateRequest;
import com.example.becommerce.dto.request.admin.WalletAdjustRequest;
import com.example.becommerce.dto.response.PagedResponse;
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
import com.example.becommerce.entity.SystemSetting;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.Wallet;
import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.entity.enums.TransactionStatus;
import com.example.becommerce.entity.enums.TransactionType;
import com.example.becommerce.entity.enums.UserStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.CategoryRepository;
import com.example.becommerce.repository.SystemSettingRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.repository.WalletRepository;
import com.example.becommerce.repository.WalletTransactionRepository;
import com.example.becommerce.service.AdminService;
import com.example.becommerce.utils.BankAccountMaskUtils;
import com.example.becommerce.utils.TransactionCodeGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String SETTINGS_KEY = "admin.settings";
    private static final String[] COLORS = {"#3b82f6", "#8b5cf6", "#f59e0b", "#06b6d4", "#22c55e"};

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final TransactionCodeGenerator transactionCodeGenerator;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        BigDecimal revenue = walletTransactionRepository.sumNetAmountByStatus(TransactionStatus.SUCCESS);
        BigDecimal profit = walletTransactionRepository.sumFeeByStatus(TransactionStatus.SUCCESS);
        long activeTechnicians = userRepository.countByRoleAndStatusAndDeletedFalse(Role.TECHNICIAN, UserStatus.ACTIVE);

        return AdminStatsResponse.builder()
                .totalRevenue(metric(revenue, BigDecimal.ZERO))
                .totalProfit(metric(profit, BigDecimal.ZERO))
                .activeTechnicians(metric(BigDecimal.valueOf(activeTechnicians), BigDecimal.ZERO))
                .ordersToday(metric(BigDecimal.ZERO, BigDecimal.ZERO))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueStatsResponse getRevenueStats(String range) {
        int days = parseRangeDays(range);
        LocalDate startDate = LocalDate.now().minusDays(days - 1L);
        List<RevenueStatsResponse.Item> items = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            BigDecimal value = walletTransactionRepository.sumNetAmountByStatusAndCreatedAtBetween(
                    TransactionStatus.SUCCESS, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            items.add(RevenueStatsResponse.Item.builder()
                    .label(labelOf(date))
                    .value(value)
                    .date(date)
                    .build());
        }
        return RevenueStatsResponse.builder()
                .range(StringUtils.hasText(range) ? range : "7days")
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDistributionResponse getServiceDistribution() {
        var categories = categoryRepository.findByDeletedFalseOrderByPriorityDescCreatedAtDesc();
        int total = Math.max(categories.size(), 1);
        List<ServiceDistributionResponse.Item> items = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            items.add(ServiceDistributionResponse.Item.builder()
                    .name(categories.get(i).getTitle())
                    .percentage(BigDecimal.valueOf(100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP))
                    .color(COLORS[i % COLORS.length])
                    .build());
        }
        return ServiceDistributionResponse.builder().items(items).build();
    }

    @Override
    public RecentOrdersResponse getRecentOrders(int limit) {
        return RecentOrdersResponse.builder().items(List.of()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTransactionsResponse getTransactions(String type, String date, int page, int limit) {
        Page<WalletTransaction> transactionPage = walletTransactionRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(type) && !"all".equalsIgnoreCase(type)) {
                predicates.add(cb.equal(root.get("type"), parseTransactionType(type)));
            }
            if (StringUtils.hasText(date)) {
                LocalDate localDate = LocalDate.parse(date);
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), localDate.atStartOfDay()));
                predicates.add(cb.lessThan(root.get("createdAt"), localDate.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        }, PageRequest.of(Math.max(0, page - 1), Math.max(1, limit), Sort.by(Sort.Direction.DESC, "createdAt")));

        List<AdminTransactionsResponse.Item> items = transactionPage.getContent().stream()
                .map(this::toTransactionItem)
                .toList();

        return AdminTransactionsResponse.builder()
                .totalBalance(walletRepository.sumBalance())
                .items(items)
                .pagination(PagedResponse.PaginationMeta.builder()
                        .page(page)
                        .limit(limit)
                        .total(transactionPage.getTotalElements())
                        .totalPages(transactionPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WithdrawRequestsResponse getWithdrawRequests() {
        List<WalletTransaction> withdrawals = walletTransactionRepository.findByTypeOrderByCreatedAtDesc(TransactionType.WITHDRAW);
        return WithdrawRequestsResponse.builder()
                .pendingCount(walletTransactionRepository.countByTypeAndStatus(TransactionType.WITHDRAW, TransactionStatus.PENDING))
                .items(withdrawals.stream().map(this::toWithdrawItem).toList())
                .build();
    }

    @Override
    @Transactional
    public WithdrawApproveResponse approveWithdrawRequest(String id) {
        WalletTransaction transaction = walletTransactionRepository.findByTransactionCode(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy yêu cầu rút tiền"));
        if (transaction.getType() != TransactionType.WITHDRAW || transaction.getStatus() != TransactionStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.INVALID_WITHDRAW_REQUEST, "Yêu cầu rút tiền không hợp lệ hoặc đã xử lý");
        }

        Wallet wallet = walletRepository.findWithLockByUser_Id(transaction.getWallet().getUser().getId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy ví"));
        wallet.setPendingBalance(wallet.getPendingBalance().subtract(transaction.getNetAmount()));
        walletRepository.save(wallet);

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setProcessedAt(LocalDateTime.now());
        walletTransactionRepository.save(transaction);

        return WithdrawApproveResponse.builder()
                .id(transaction.getTransactionCode())
                .status("approved")
                .processedAt(transaction.getProcessedAt())
                .processedBy(currentAdminLabel())
                .build();
    }

    @Override
    @Transactional
    public CommissionResponse updateCommission(CommissionUpdateRequest request) {
        AdminSettingsRequest settings = getSettings();
        settings.getBilling().setPlatformFeePercent(request.getPlatformFeePercent());
        settings.getBilling().setVatPercent(request.getVatPercent());
        updateSettings(settings);
        return CommissionResponse.builder()
                .platformFeePercent(request.getPlatformFeePercent())
                .vatPercent(request.getVatPercent())
                .updatedBy(currentAdminLabel())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public WalletAdjustResponse adjustWallet(WalletAdjustRequest request) {
        User technician = userRepository.findByCodeAndDeletedFalse(request.getTechnicianId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy kỹ thuật viên"));
        Wallet wallet = walletRepository.findWithLockByUser_Id(technician.getId())
                .orElseGet(() -> walletRepository.save(Wallet.builder().user(technician).currency("VND").build()));
        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.badRequest(ErrorCode.INSUFFICIENT_BALANCE, "Số dư ví không đủ để điều chỉnh");
        }
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .transactionCode(transactionCodeGenerator.generateTransactionCode(TransactionType.COMMISSION))
                .wallet(wallet)
                .type(TransactionType.COMMISSION)
                .category(request.getType())
                .title(request.getReason())
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .netAmount(request.getAmount())
                .status(TransactionStatus.SUCCESS)
                .processedAt(LocalDateTime.now())
                .build();
        walletTransactionRepository.save(transaction);

        return WalletAdjustResponse.builder()
                .transactionId(transaction.getTransactionCode())
                .technicianId(technician.getCode())
                .amount(request.getAmount())
                .newBalance(wallet.getBalance())
                .reason(request.getReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSettingsRequest getSettings() {
        return systemSettingRepository.findByKey(SETTINGS_KEY)
                .map(setting -> readSettings(setting.getValue()))
                .orElseGet(this::defaultSettings);
    }

    @Override
    @Transactional
    public AdminSettingsSavedResponse updateSettings(AdminSettingsRequest request) {
        String json = writeSettings(request);
        SystemSetting setting = systemSettingRepository.findByKey(SETTINGS_KEY)
                .orElse(SystemSetting.builder().key(SETTINGS_KEY).value(json).build());
        setting.setValue(json);
        systemSettingRepository.save(setting);
        return AdminSettingsSavedResponse.builder()
                .message("Cấu hình đã được lưu")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private AdminStatsResponse.Metric metric(BigDecimal value, BigDecimal change) {
        return AdminStatsResponse.Metric.builder()
                .value(value)
                .change(change)
                .changeDirection(change.compareTo(BigDecimal.ZERO) > 0 ? "up" : change.compareTo(BigDecimal.ZERO) < 0 ? "down" : "neutral")
                .build();
    }

    private AdminTransactionsResponse.Item toTransactionItem(WalletTransaction transaction) {
        User user = transaction.getWallet().getUser();
        return AdminTransactionsResponse.Item.builder()
                .id(transaction.getTransactionCode())
                .time(transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .date(transaction.getCreatedAt().toLocalDate().toString())
                .partner(AdminTransactionsResponse.Partner.builder()
                        .name(user.getFullName())
                        .area(String.join(", ", List.of(nullToEmpty(user.getDistrict()), nullToEmpty(user.getAddress()))).replaceAll("(^, |, $)", ""))
                        .build())
                .type(transaction.getType().apiValue())
                .amount(transaction.getAmount())
                .status(transaction.getStatus() == TransactionStatus.SUCCESS ? "done" : transaction.getStatus().apiValue())
                .build();
    }

    private WithdrawRequestsResponse.Item toWithdrawItem(WalletTransaction transaction) {
        User user = transaction.getWallet().getUser();
        return WithdrawRequestsResponse.Item.builder()
                .id(transaction.getTransactionCode())
                .technician(WithdrawRequestsResponse.Technician.builder()
                        .id(user.getCode())
                        .fullName(user.getFullName())
                        .avatar(StringUtils.hasText(user.getAvatar()) ? user.getAvatar() : initials(user.getFullName()))
                        .build())
                .amount(transaction.getAmount())
                .bankName(transaction.getBankAccount() == null ? null : transaction.getBankAccount().getBankName())
                .accountNumber(transaction.getBankAccount() == null ? null : BankAccountMaskUtils.mask(transaction.getBankAccount().getAccountNumber()))
                .requestedAt(transaction.getCreatedAt())
                .status(transaction.getStatus().apiValue())
                .build();
    }

    private TransactionType parseTransactionType(String value) {
        try {
            return TransactionType.from(value);
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest(ErrorCode.INVALID_TRANSACTION_TYPE, "Loại giao dịch không hợp lệ");
        }
    }

    private int parseRangeDays(String range) {
        if (!StringUtils.hasText(range)) {
            return 7;
        }
        String normalized = range.toLowerCase(Locale.ROOT).replace("days", "");
        try {
            return Math.min(Math.max(Integer.parseInt(normalized), 1), 90);
        } catch (NumberFormatException ex) {
            return 7;
        }
    }

    private String labelOf(LocalDate date) {
        String[] labels = {"CN", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"};
        return labels[date.getDayOfWeek().getValue() % 7];
    }

    private String currentAdminLabel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "Admin" : "Admin " + authentication.getName();
    }

    private AdminSettingsRequest readSettings(String json) {
        try {
            return objectMapper.readValue(json, AdminSettingsRequest.class);
        } catch (JsonProcessingException ex) {
            return defaultSettings();
        }
    }

    private String writeSettings(AdminSettingsRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Cấu hình không hợp lệ");
        }
    }

    private AdminSettingsRequest defaultSettings() {
        AdminSettingsRequest request = new AdminSettingsRequest();
        AdminSettingsRequest.General general = new AdminSettingsRequest.General();
        general.setAppName("GlowUp Concierge");
        general.setHotline("1900 8888");
        general.setEmail("admin@glowup.vn");
        request.setGeneral(general);

        AdminSettingsRequest.Billing billing = new AdminSettingsRequest.Billing();
        billing.setPlatformFeePercent(BigDecimal.valueOf(15));
        billing.setVatPercent(BigDecimal.valueOf(10));
        request.setBilling(billing);

        AdminSettingsRequest.Notifications notifications = new AdminSettingsRequest.Notifications();
        notifications.setNewOrder(true);
        notifications.setCustomerEmail(true);
        notifications.setWeeklyRevenue(false);
        notifications.setSecurityAlert(true);
        request.setNotifications(notifications);

        AdminSettingsRequest.Operations operations = new AdminSettingsRequest.Operations();
        operations.setRequireManualReview(true);
        operations.setTechnicianAutoPause(false);
        operations.setIncidentEscalation(true);
        request.setOperations(operations);
        return request;
    }

    private String initials(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return "";
        }
        String[] words = fullName.trim().split("\\s+");
        return words.length == 1 ? words[0].substring(0, 1).toUpperCase(Locale.ROOT)
                : (words[0].substring(0, 1) + words[words.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
