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
import com.example.becommerce.dto.response.admin.CommissionSettingsResponse;
import com.example.becommerce.dto.response.admin.CommissionWalletsResponse;
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
import com.example.becommerce.entity.enums.WalletStatus;
import com.example.becommerce.entity.enums.WalletType;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.CategoryRepository;
import com.example.becommerce.repository.OrderRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String SETTINGS_KEY = "admin.settings";
    private static final String FIXED_COMMISSION_FEE_KEY = "fixed_commission_fee";
    private static final String MINIMUM_COMMISSION_BALANCE_KEY = "minimum_commission_balance";
    private static final String AUTO_LOCK_ENABLED_KEY = "auto_lock_enabled";
    private static final String[] COLORS = {"#3b82f6", "#8b5cf6", "#f59e0b", "#06b6d4", "#22c55e"};

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final TransactionCodeGenerator transactionCodeGenerator;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats(String mode, Integer year, Integer quarter, Integer month) {
        var range = computeRange(mode, year, quarter, month);
        LocalDateTime from = range[0];
        LocalDateTime to = range[1];

        long completedCount = orderRepository.countByCompletedAtBetweenAndDeletedFalse(from, to);

        // Sum revenue by aggregating group queries (works for month/day ranges)
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if ("month".equalsIgnoreCase(mode)) {
            List<Object[]> rows = orderRepository.sumFinalPriceGroupByDay(from, to);
            for (Object[] row : rows) {
                Object val = row[1];
                totalRevenue = totalRevenue.add(toBigDecimal(val));
            }
        } else {
            List<Object[]> rows = orderRepository.sumFinalPriceGroupByMonth(from, to);
            for (Object[] row : rows) {
                Object val = row[1];
                totalRevenue = totalRevenue.add(toBigDecimal(val));
            }
        }

        BigDecimal totalProfit = BigDecimal.valueOf(10000).multiply(BigDecimal.valueOf(completedCount));
        long activeTechnicians = userRepository.countByRoleAndStatusAndDeletedFalse(Role.TECHNICIAN, UserStatus.ACTIVE);

        return AdminStatsResponse.builder()
                .totalRevenue(metric(totalRevenue, BigDecimal.ZERO))
                .totalProfit(metric(totalProfit, BigDecimal.ZERO))
                .activeTechnicians(metric(BigDecimal.valueOf(activeTechnicians), BigDecimal.ZERO))
                .totalOrders(metric(BigDecimal.valueOf(completedCount), BigDecimal.ZERO))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueStatsResponse getRevenueStats(String mode, Integer year, Integer quarter, Integer month) {
        mode = StringUtils.hasText(mode) ? mode.toLowerCase() : "month";

        var range = computeRange(mode, year, quarter, month);
        LocalDateTime from = range[0];
        LocalDateTime to = range[1];

        String rangeLabel;
        List<RevenueStatsResponse.Item> items = new ArrayList<>();

        if ("all-time".equals(mode)) {
            rangeLabel = "All-time";
            List<Object[]> results = orderRepository.sumFinalPriceGroupByMonth(from, to);
            for (Object[] row : results) {
                String monthStr = (String) row[0];
                BigDecimal value = toBigDecimal(row[1]);
                YearMonth ym = YearMonth.parse(monthStr);
                String label = "Thg " + ym.getMonthValue() + " " + ym.getYear();
                items.add(RevenueStatsResponse.Item.builder()
                        .label(label)
                        .date(ym.atDay(1).atStartOfDay().toLocalDate())
                        .value(value)
                        .build());
            }
        } else if ("year".equals(mode) && year != null) {
            rangeLabel = "Year " + year;
            List<Object[]> results = orderRepository.sumFinalPriceGroupByMonth(from, to);
            Map<Integer, BigDecimal> monthMap = new TreeMap<>();
            for (Object[] row : results) {
                String monthStr = (String) row[0];
                BigDecimal value = toBigDecimal(row[1]);
                YearMonth ym = YearMonth.parse(monthStr);
                monthMap.put(ym.getMonthValue(), value);
            }
            for (int m = 1; m <= 12; m++) {
                BigDecimal value = monthMap.getOrDefault(m, BigDecimal.ZERO);
                String label = "Thg " + m;
                items.add(RevenueStatsResponse.Item.builder()
                        .label(label)
                        .date(LocalDate.of(year, m, 1))
                        .value(value)
                        .build());
            }
        } else if ("quarter".equals(mode) && year != null && quarter != null) {
            rangeLabel = "Q" + quarter + " " + year;
            List<Object[]> results = orderRepository.sumFinalPriceGroupByMonth(from, to);
            Map<Integer, BigDecimal> monthMap = new TreeMap<>();
            for (Object[] row : results) {
                String monthStr = (String) row[0];
                BigDecimal value = toBigDecimal(row[1]);
                YearMonth ym = YearMonth.parse(monthStr);
                monthMap.put(ym.getMonthValue(), value);
            }
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = startMonth + 2;
            for (int m = startMonth; m <= endMonth; m++) {
                BigDecimal value = monthMap.getOrDefault(m, BigDecimal.ZERO);
                String label = "Thg " + m;
                items.add(RevenueStatsResponse.Item.builder()
                        .label(label)
                        .date(LocalDate.of(year, m, 1))
                        .value(value)
                        .build());
            }
        } else {
            // month
            int y = year != null ? year : LocalDate.now().getYear();
            int m = month != null ? month : LocalDate.now().getMonthValue();
            LocalDate firstDay = LocalDate.of(y, m, 1);
            int daysInMonth = firstDay.lengthOfMonth();
            rangeLabel = "Month " + m + "/" + y;
            List<Object[]> results = orderRepository.sumFinalPriceGroupByDay(from, to);
            Map<Integer, BigDecimal> dayMap = new TreeMap<>();
            for (Object[] row : results) {
                String dayStr = (String) row[0];
                BigDecimal value = toBigDecimal(row[1]);
                LocalDate date = LocalDate.parse(dayStr);
                dayMap.put(date.getDayOfMonth(), value);
            }
            for (int d = 1; d <= daysInMonth; d++) {
                BigDecimal value = dayMap.getOrDefault(d, BigDecimal.ZERO);
                LocalDate date = LocalDate.of(y, m, d);
                String label = "Ngày " + d;
                items.add(RevenueStatsResponse.Item.builder()
                        .label(label)
                        .date(date)
                        .value(value)
                        .build());
            }
        }

        return RevenueStatsResponse.builder()
                .range(rangeLabel)
                .items(items)
                .build();
    }

    public AdminStatsResponse getStats() {
        // delegate through proxy so @Transactional on the parameterized method is honored
        AdminService proxy = applicationContext.getBean(AdminService.class);
        return proxy.getStats("month", null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDistributionResponse getServiceDistribution(String mode, Integer year, Integer quarter, Integer month) {
        mode = StringUtils.hasText(mode) ? mode.toLowerCase() : "month";
        var range = computeRange(mode, year, quarter, month);
        LocalDateTime from = range[0];
        LocalDateTime to = range[1];

        long totalOrders = orderRepository.countByCompletedAtBetweenAndDeletedFalse(from, to);
        if (totalOrders == 0) {
            return ServiceDistributionResponse.builder().items(new ArrayList<>()).build();
        }

        var categories = categoryRepository.findByDeletedFalseOrderByPriorityDescCreatedAtDesc();
        List<ServiceDistributionResponse.Item> items = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            var category = categories.get(i);
            long count = orderRepository.countByCategoryIdAndCompletedAtBetweenAndDeletedFalse(category.getId(), from, to);
            if (count > 0) {
                BigDecimal percentage = BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
                items.add(ServiceDistributionResponse.Item.builder()
                        .name(category.getTitle())
                        .percentage(percentage)
                        .color(COLORS[i % COLORS.length])
                        .build());
            }
        }
        return ServiceDistributionResponse.builder().items(items).build();
    }

    @Override
    @Transactional(readOnly = true)
    public RecentOrdersResponse getRecentOrders(String mode, Integer year, Integer quarter, Integer month, int limit) {
        var range = computeRange(mode, year, quarter, month);
        LocalDateTime from = range[0];
        LocalDateTime to = range[1];

        Pageable pageable = PageRequest.of(0, Math.max(1, limit), Sort.by(Sort.Direction.DESC, "scheduledAt"));
        Page<com.example.becommerce.entity.Order> orderPage = orderRepository.findByCompletedAtBetweenAndDeletedFalseOrderByScheduledAtDesc(from, to, pageable);

        List<RecentOrdersResponse.Item> items = orderPage.getContent().stream()
                .map(order -> RecentOrdersResponse.Item.builder()
                        .id(order.getCode())
                        .customer(RecentOrdersResponse.Person.builder()
                                .fullName(order.getCustomer().getFullName())
                                .build())
                        .technician(order.getTechnician() != null ? RecentOrdersResponse.Person.builder()
                                .fullName(order.getTechnician().getFullName())
                                .build() : null)
                        .serviceName(order.getServiceName())
                        .status(order.getStatus().apiValue())
                        .scheduledAt(order.getScheduledAt())
                        .amount(BigDecimal.valueOf(order.getFinalPrice() != null ? order.getFinalPrice() : 0))
                        .build())
                .toList();

        return RecentOrdersResponse.builder().items(items).build();
    }

    // helper: compute range from mode/year/quarter/month
    private LocalDateTime[] computeRange(String mode, Integer year, Integer quarter, Integer month) {
        mode = StringUtils.hasText(mode) ? mode.toLowerCase() : "month";
        LocalDateTime from;
        LocalDateTime to = LocalDateTime.now();

        if ("all-time".equals(mode)) {
            from = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        } else if ("year".equals(mode) && year != null) {
            from = LocalDateTime.of(year, 1, 1, 0, 0, 0);
            LocalDate lastDayOfYear = YearMonth.of(year, 12).atEndOfMonth();
            to = LocalDateTime.of(lastDayOfYear, java.time.LocalTime.of(23, 59, 59));
        } else if ("quarter".equals(mode) && year != null && quarter != null) {
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = startMonth + 2;
            from = LocalDateTime.of(year, startMonth, 1, 0, 0, 0);
            LocalDate lastDayOfQuarter = YearMonth.of(year, endMonth).atEndOfMonth();
            to = LocalDateTime.of(lastDayOfQuarter, java.time.LocalTime.of(23, 59, 59));
        } else {
            int y = year != null ? year : LocalDate.now().getYear();
            int m = month != null ? month : LocalDate.now().getMonthValue();
            from = LocalDateTime.of(y, m, 1, 0, 0, 0);
            LocalDate lastDayOfMonth = YearMonth.of(y, m).atEndOfMonth();
            to = LocalDateTime.of(lastDayOfMonth, java.time.LocalTime.of(23, 59, 59));
        }
        return new LocalDateTime[]{from, to};
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).longValue());
        if (val == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val.toString());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
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
        wallet.normalizeForPersistence();
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
        saveSystemSetting(FIXED_COMMISSION_FEE_KEY, request.getFixedCommissionFee().toPlainString());
        saveSystemSetting(MINIMUM_COMMISSION_BALANCE_KEY, request.getMinimumCommissionBalance().toPlainString());
        return CommissionResponse.builder()
                .fixedCommissionFee(request.getFixedCommissionFee())
                .minimumCommissionBalance(request.getMinimumCommissionBalance())
                .updatedBy(currentAdminLabel())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionSettingsResponse getCommissionSettings() {
        BigDecimal fixedFee = BigDecimal.ZERO;
        BigDecimal minBalance = BigDecimal.ZERO;
        Boolean autoLock = false;
        LocalDateTime updatedAt = LocalDateTime.now();

        // Retrieve fixed commission fee
        var fixedFeeSetting = systemSettingRepository.findByKey(FIXED_COMMISSION_FEE_KEY);
        if (fixedFeeSetting.isPresent()) {
            try {
                fixedFee = new BigDecimal(fixedFeeSetting.get().getValue());
            } catch (Exception ex) {
                fixedFee = BigDecimal.ZERO;
            }
        }

        // Retrieve minimum commission balance
        var minBalanceSetting = systemSettingRepository.findByKey(MINIMUM_COMMISSION_BALANCE_KEY);
        if (minBalanceSetting.isPresent()) {
            try {
                minBalance = new BigDecimal(minBalanceSetting.get().getValue());
            } catch (Exception ex) {
                minBalance = BigDecimal.ZERO;
            }
        }

        // Retrieve auto lock enabled
        var autoLockSetting = systemSettingRepository.findByKey(AUTO_LOCK_ENABLED_KEY);
        if (autoLockSetting.isPresent()) {
            autoLock = Boolean.parseBoolean(autoLockSetting.get().getValue());
        }

        return CommissionSettingsResponse.builder()
                .fixedCommissionFee(fixedFee)
                .minimumCommissionBalance(minBalance)
                .autoLockEnabled(autoLock)
                .updatedAt(updatedAt)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionWalletsResponse getCommissionWallets(String status, String keyword, int page, int size) {
        BigDecimal minimumCommissionBalance = getMinimumCommissionBalance();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), Sort.by(Sort.Direction.DESC, "balance"));

        Page<Wallet> walletPage = walletRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by computed status if provided
            if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status)) {
                try {
                    WalletStatus walletStatus = parseWalletStatus(status);
                    predicates.addAll(buildWalletStatusPredicates(cb, root, walletStatus, minimumCommissionBalance));
                } catch (IllegalArgumentException ex) {
                    throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Trạng thái ví không hợp lệ");
                }
            }

            // Filter by technician name (keyword)
            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(cb.lower(root.get("user").get("fullName")),
                        "%" + keyword.toLowerCase() + "%"));
            }

            // Only include technician wallets
            predicates.add(cb.equal(root.get("user").get("role"), Role.TECHNICIAN));
            predicates.add(cb.equal(root.get("user").get("deleted"), false));

            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);

        List<CommissionWalletsResponse.Item> items = walletPage.getContent().stream()
                .map(wallet -> toCommissionWalletItem(wallet, minimumCommissionBalance))
                .toList();

        return CommissionWalletsResponse.builder()
                .content(items)
                .pagination(PagedResponse.PaginationMeta.builder()
                        .page(page)
                        .limit(size)
                        .total(walletPage.getTotalElements())
                        .totalPages(walletPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public WalletAdjustResponse adjustWallet(WalletAdjustRequest request) {
        User technician = userRepository.findByCodeAndDeletedFalse(request.getTechnicianId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy kỹ thuật viên"));
        Wallet wallet = walletRepository.findWithLockByUser_Id(technician.getId())
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .user(technician)
                        .balance(BigDecimal.ZERO)
                        .personalBalance(BigDecimal.ZERO)
                        .currency("VND")
                        .build()));
        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.badRequest(ErrorCode.INSUFFICIENT_BALANCE, "Số dư ví không đủ để điều chỉnh");
        }
        wallet.normalizeForPersistence();
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .transactionCode(transactionCodeGenerator.generateTransactionCode(TransactionType.COMMISSION))
                .wallet(wallet)
                .type(TransactionType.COMMISSION)
                .walletType(WalletType.CREDIT)
                .category(request.getType())
                .title(request.getReason())
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .netAmount(request.getAmount())
                .afterBalance(newBalance.longValueExact())
                .note(request.getReason())
                .actor(currentAdminLabel())
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
        String orderCode = transaction.getOrder() != null ? transaction.getOrder().getCode() : (transaction.getRelatedOrderCode() != null ? transaction.getRelatedOrderCode() : "");
        WalletStatus walletStatus = resolveCommissionWalletStatus(transaction.getWallet().getBalance(), getMinimumCommissionBalance());

        return AdminTransactionsResponse.Item.builder()
                .id(transaction.getTransactionCode())
                .transactionCode(transaction.getTransactionCode())
                .transactionType(transaction.getType().apiValue())
                .amount(transaction.getAmount())
                .afterBalance(transaction.getAfterBalance())
                .walletStatus(walletStatus.apiValue())
                .technicianName(user.getFullName())
                .orderCode(orderCode)
                .note(transaction.getNote())
                .actor(transaction.getActor())
                .createdAt(transaction.getCreatedAt())
                // Legacy fields
                .time(transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .date(transaction.getCreatedAt().toLocalDate().toString())
                .partner(AdminTransactionsResponse.Partner.builder()
                        .name(user.getFullName())
                        .area(String.join(", ", List.of(nullToEmpty(user.getDistrict()), nullToEmpty(user.getAddress()))).replaceAll("(^, |, $)", ""))
                        .build())
                .type(transaction.getType().apiValue())
                .status(transaction.getStatus() == TransactionStatus.SUCCESS ? "done" : transaction.getStatus().apiValue())
                .build();
    }

    private CommissionWalletsResponse.Item toCommissionWalletItem(Wallet wallet, BigDecimal minimumCommissionBalance) {
        BigDecimal totalCommissionPaid = walletTransactionRepository.sumCommissionDeductionAmount(wallet.getId());
        totalCommissionPaid = totalCommissionPaid == null ? BigDecimal.ZERO : totalCommissionPaid.abs();

        LocalDateTime lastOrderAt = walletTransactionRepository.findLastOrderActivityAt(wallet.getId());
        WalletStatus walletStatus = resolveCommissionWalletStatus(wallet.getBalance(), minimumCommissionBalance);

        return CommissionWalletsResponse.Item.builder()
                .technicianId(wallet.getUser().getId())
                .technicianName(wallet.getUser().getFullName())
                .walletBalance(wallet.getBalance())
                .walletStatus(walletStatus.apiValue())
                .totalCommissionPaid(totalCommissionPaid)
                .lastOrderAt(lastOrderAt)
                .locked(walletStatus == WalletStatus.LOCKED)
                .build();
    }

    private WalletStatus resolveCommissionWalletStatus(BigDecimal balance, BigDecimal minimumCommissionBalance) {
        BigDecimal safeBalance = balance == null ? BigDecimal.ZERO : balance;
        BigDecimal threshold = minimumCommissionBalance == null ? BigDecimal.ZERO : minimumCommissionBalance;
        if (safeBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return WalletStatus.LOCKED;
        }
        if (safeBalance.compareTo(threshold) <= 0) {
            return WalletStatus.LOW_BALANCE;
        }
        return WalletStatus.NORMAL;
    }

    private BigDecimal getMinimumCommissionBalance() {
        return systemSettingRepository.findByKey(MINIMUM_COMMISSION_BALANCE_KEY)
                .map(setting -> {
                    try {
                        return new BigDecimal(setting.getValue());
                    } catch (Exception ex) {
                        return BigDecimal.ZERO;
                    }
                })
                .orElse(BigDecimal.ZERO);
    }

    private WalletStatus parseWalletStatus(String value) {
        return WalletStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private List<Predicate> buildWalletStatusPredicates(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Root<Wallet> root,
            WalletStatus walletStatus,
            BigDecimal minimumCommissionBalance) {
        List<Predicate> predicates = new ArrayList<>();
        BigDecimal safeMinimum = minimumCommissionBalance == null ? BigDecimal.ZERO : minimumCommissionBalance;

        switch (walletStatus) {
            case LOCKED -> predicates.add(cb.lessThanOrEqualTo(root.get("balance"), BigDecimal.ZERO));
            case LOW_BALANCE -> {
                predicates.add(cb.greaterThan(root.get("balance"), BigDecimal.ZERO));
                predicates.add(cb.lessThanOrEqualTo(root.get("balance"), safeMinimum));
            }
            case NORMAL -> predicates.add(cb.greaterThan(root.get("balance"), safeMinimum));
        }

        return predicates;
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
                return Integer.parseInt(normalized) < 1 ? 1 : Math.min(Integer.parseInt(normalized), 90);
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

    private void saveSystemSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findByKey(key)
                .orElse(SystemSetting.builder().key(key).build());
        setting.setValue(value);
        systemSettingRepository.save(setting);
    }

    private String initials(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return "";
        }
        String[] words = fullName.trim().split("\\s+");
        return words.length == 1 ? words[0].substring(0, 1).toUpperCase(Locale.ROOT)
                : (String.valueOf(words[0].charAt(0)) + words[words.length - 1].charAt(0)).toUpperCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
