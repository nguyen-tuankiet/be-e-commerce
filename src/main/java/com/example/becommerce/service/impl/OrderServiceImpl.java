package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.OrderMapper;
import com.example.becommerce.dto.request.order.CancelOrderRequest;
import com.example.becommerce.dto.request.order.CompleteOrderRequest;
import com.example.becommerce.dto.request.order.CreateOrderRequest;
import com.example.becommerce.dto.request.order.PriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.RejectOrderRequest;
import com.example.becommerce.dto.request.order.RejectPriceAdjustmentRequest;
import com.example.becommerce.dto.request.order.UpdateOrderStatusRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.order.OrderResponse;
import com.example.becommerce.dto.response.order.OrderStatusChangeResponse;
import com.example.becommerce.dto.response.order.PriceAdjustmentEnvelope;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.OrderImage;
import com.example.becommerce.entity.OrderPriceAdjustment;
import com.example.becommerce.entity.OrderPriceAdjustmentPart;
import com.example.becommerce.entity.OrderStatusHistory;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.Wallet;
import com.example.becommerce.entity.WalletTransaction;
import com.example.becommerce.entity.enums.OrderActor;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.PriceAdjustmentStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.entity.enums.TransactionStatus;
import com.example.becommerce.entity.enums.TransactionType;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderPriceAdjustmentRepository;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.SystemSettingRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.repository.WalletRepository;
import com.example.becommerce.repository.WalletTransactionRepository;
import com.example.becommerce.entity.enums.NotificationType;
import com.example.becommerce.service.NotificationService;
import com.example.becommerce.service.OrderService;
import com.example.becommerce.service.WsEventPublisher;
import com.example.becommerce.utils.OrderCodeGenerator;
import com.example.becommerce.utils.OrderSpecification;
import com.example.becommerce.utils.TransactionCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order business logic.
 *
 * <p>Authorization model
 *  <ul>
 *    <li>Customer: can list/read only own orders, can create, cancel, approve/reject price adjustments.</li>
 *    <li>Technician: can list orders assigned to them or in the open pool, can accept/reject/in-progress/complete,
 *        can request a price adjustment.</li>
 *    <li>Admin: can list/read everything; status changes go through specific endpoints.</li>
 *  </ul>
 *
 * <p>Status transition rules are enforced inline at each method to keep the
 * intent close to the endpoint that triggers it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository                 orderRepository;
    private final OrderPriceAdjustmentRepository  adjustmentRepository;
    private final UserRepository                  userRepository;
    private final WalletRepository                walletRepository;
    private final WalletTransactionRepository     walletTransactionRepository;
    private final SystemSettingRepository         systemSettingRepository;
    private final OrderMapper                     orderMapper;
    private final OrderCodeGenerator              codeGenerator;
    private final TransactionCodeGenerator        transactionCodeGenerator;
    private final WsEventPublisher                eventPublisher;
    private final NotificationService             notificationService;

    // ===============================================================
    // LIST + DETAIL
    // ===============================================================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrders(String status, String keyword, int page, int limit) {
        User current = getCurrentUser();
        Long customerId  = current.getRole() == Role.CUSTOMER   ? current.getId() : null;
        Long technicianId = current.getRole() == Role.TECHNICIAN ? current.getId() : null;

        Specification<Order> spec = OrderSpecification.buildFilter(status, customerId, technicianId, keyword);

        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> items = ordersPage.getContent().stream()
                .map(orderMapper::toListItem)
                .toList();

        return PagedResponse.of(items, page, limit, ordersPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String code) {
        Order order = findOrder(code);
        ensureCanRead(order, getCurrentUser());
        return orderMapper.toDetailResponse(order);
    }

    // ===============================================================
    // CREATE (customer)
    // ===============================================================

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User customer = getCurrentUser();
        if (customer.getRole() != Role.CUSTOMER) {
            throw AppException.forbidden("Chỉ khách hàng mới được tạo đơn");
        }

        User technician = null;
        if (request.getTechnicianId() != null && !request.getTechnicianId().isBlank()) {
            technician = userRepository.findByCodeAndDeletedFalse(request.getTechnicianId())
                    .orElseThrow(() -> AppException.notFound("Không tìm thấy kỹ thuật viên " + request.getTechnicianId()));
            if (technician.getRole() != Role.TECHNICIAN) {
                throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Tài khoản không phải kỹ thuật viên");
            }
        }

        Order order = Order.builder()
                .code(codeGenerator.generate())
                .customer(customer)
                .technician(technician)
                .deviceName(request.getDeviceName())
                .description(request.getDescription())
                .address(request.getAddress())
                .estimatedPrice(request.getEstimatedPrice())
                .expectedTime(request.getExpectedTime())
                .serviceCategory(request.getServiceCategory())
                .serviceName(request.getServiceName())
                .subService(request.getSubService())
                .status(OrderStatus.NEW)
                .build();

        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                if (url == null || url.isBlank()) continue;
                order.getImages().add(OrderImage.builder()
                        .order(order)
                        .url(url.trim())
                        .role("request")
                        .build());
            }
        }

        recordHistory(order, null, OrderStatus.NEW, OrderActor.CUSTOMER, customer.getId(), "Khách hàng tạo đơn");

        Order saved = orderRepository.save(order);
        log.info("Order created: {} by customer {}", saved.getCode(), customer.getCode());
        return orderMapper.toDetailResponse(saved);
    }

    // ===============================================================
    // CANCEL (customer)
    // ===============================================================

    @Override
    @Transactional
    public OrderStatusChangeResponse cancelOrder(String code, CancelOrderRequest request) {
        Order order = findOrder(code);
        User current = getCurrentUser();
        OrderActor actor = ensureCanCancel(order, current);

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Đơn ở trạng thái này không thể hủy");
        }

        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy(actor);
        order.setCancelReason(request.getReason());

        recordHistory(order, from, OrderStatus.CANCELLED, actor, current.getId(), request.getReason());

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderStatusChanged(
                saved.getCode(), from.apiValue(), OrderStatus.CANCELLED.apiValue());
        return orderMapper.toStatusChange(saved);
    }

    // ===============================================================
    // ACCEPT (technician)
    // ===============================================================

    @Override
    @Transactional
    public OrderStatusChangeResponse acceptOrder(String code) {
        Order order = findOrder(code);
        User technician = getCurrentUser();

        if (technician.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể nhận đơn");
        }
        Wallet wallet = walletRepository.findByUser_Id(technician.getId()).orElse(null);
        if (wallet == null) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Không tìm thấy ví hoa hồng của kỹ thuật viên");
        }
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.forbidden("Ví hoa hồng đang bị khóa do số dư thấp, không thể nhận đơn");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw AppException.conflict(ErrorCode.ORDER_ALREADY_TAKEN, "Đơn này đã có thợ nhận hoặc không còn nhận được");
        }

        order.setTechnician(technician);
        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.SCHEDULED);
        if (order.getScheduledAt() == null && order.getExpectedTime() != null) {
            order.setScheduledAt(order.getExpectedTime());
        }

        recordHistory(order, from, OrderStatus.SCHEDULED, OrderActor.TECHNICIAN, technician.getId(), "Thợ nhận đơn");

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderStatusChanged(
                saved.getCode(), from.apiValue(), OrderStatus.SCHEDULED.apiValue());
        notifyOrderEvent(saved.getCustomer(), NotificationType.ORDER_ACCEPTED,
                "Đơn hàng được nhận",
                "Thợ " + technician.getFullName() + " đã nhận đơn " + saved.getCode(),
                saved.getCode());
        return orderMapper.toStatusChange(saved);
    }

    // ===============================================================
    // REJECT (technician → back to pool)
    // ===============================================================

    @Override
    @Transactional
    public OrderStatusChangeResponse rejectOrder(String code, RejectOrderRequest request) {
        Order order = findOrder(code);
        User technician = getCurrentUser();

        if (technician.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể từ chối đơn");
        }
        if (order.getTechnician() == null || !order.getTechnician().getId().equals(technician.getId())) {
            throw AppException.forbidden("Bạn không phải thợ phụ trách đơn này");
        }
        if (order.getStatus() != OrderStatus.SCHEDULED && order.getStatus() != OrderStatus.ASSIGNED) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Chỉ có thể từ chối đơn đã nhận, chưa bắt đầu");
        }

        OrderStatus from = order.getStatus();
        order.setTechnician(null);
        order.setStatus(OrderStatus.NEW);
        order.setScheduledAt(null);

        recordHistory(order, from, OrderStatus.NEW, OrderActor.TECHNICIAN, technician.getId(), request.getReason());

        Order saved = orderRepository.save(order);
        OrderStatusChangeResponse base = orderMapper.toStatusChange(saved);

        eventPublisher.publishOrderStatusChanged(
                saved.getCode(), from.apiValue(), OrderStatus.NEW.apiValue());

        return OrderStatusChangeResponse.builder()
                .id(base.getId())
                .status(base.getStatus())
                .updatedAt(base.getUpdatedAt())
                .message("Đơn đã được trả về pool để thợ khác nhận")
                .build();
    }

    // ===============================================================
    // UPDATE STATUS (technician — generic transition, e.g. → in_progress)
    // ===============================================================

    @Override
    @Transactional
    public OrderStatusChangeResponse updateStatus(String code, UpdateOrderStatusRequest request) {
        Order order = findOrder(code);
        User current = getCurrentUser();

        OrderStatus target;
        try {
            target = OrderStatus.from(request.getStatus());
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS, "Trạng thái không hợp lệ");
        }
        if (target == null) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS, "Trạng thái không hợp lệ");
        }

        ensureCanTransition(order, current, target);

        OrderStatus from = order.getStatus();
        order.setStatus(target);

        if (target == OrderStatus.IN_PROGRESS && order.getStartedAt() == null) {
            order.setStartedAt(LocalDateTime.now());
        }
        if (target == OrderStatus.COMPLETED && order.getCompletedAt() == null) {
            order.setCompletedAt(LocalDateTime.now());
        }

        OrderActor actor = current.getRole() == Role.TECHNICIAN ? OrderActor.TECHNICIAN
                : current.getRole() == Role.CUSTOMER ? OrderActor.CUSTOMER : OrderActor.ADMIN;
        recordHistory(order, from, target, actor, current.getId(), null);

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderStatusChanged(
                saved.getCode(), from.apiValue(), target.apiValue());
        return orderMapper.toStatusChange(saved);
    }

    // ===============================================================
    // COMPLETE (technician)
    // ===============================================================

    @Override
    @Transactional
    public OrderStatusChangeResponse completeOrder(String code, CompleteOrderRequest request) {
        Order order = findOrder(code);
        User technician = getCurrentUser();

        if (technician.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể hoàn thành đơn");
        }
        if (order.getTechnician() == null || !order.getTechnician().getId().equals(technician.getId())) {
            throw AppException.forbidden("Bạn không phải thợ phụ trách đơn này");
        }
        if (order.getStatus() != OrderStatus.IN_PROGRESS && order.getStatus() != OrderStatus.SCHEDULED) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Đơn phải ở trạng thái đang xử lý mới có thể hoàn thành");
        }

        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        if (request.getFinalPrice() != null) {
            order.setFinalPrice(request.getFinalPrice());
        } else if (order.getFinalPrice() == null) {
            order.setFinalPrice(order.getEstimatedPrice());
        }

        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                if (url == null || url.isBlank()) continue;
                order.getImages().add(OrderImage.builder()
                        .order(order)
                        .url(url.trim())
                        .role("completion")
                        .build());
            }
        }

        recordHistory(order, from, OrderStatus.COMPLETED, OrderActor.TECHNICIAN, technician.getId(), "Hoàn thành đơn");

        // Process commission logic
        processOrderCompletion(order);

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderStatusChanged(
                saved.getCode(), from.apiValue(), OrderStatus.COMPLETED.apiValue());
        notifyOrderEvent(saved.getCustomer(), NotificationType.ORDER_COMPLETED,
                "Đơn hoàn thành",
                "Đơn " + saved.getCode() + " đã hoàn thành",
                saved.getCode());
        return orderMapper.toStatusChange(saved);
    }

    /**
     * Process commission and revenue for a completed order.
     * Logic:
     * 1. Add final price as revenue to technician wallet
     * 2. Deduct fixed commission fee
     * 3. Record both transactions
     * 4. Update wallet status based on balance
     * 5. Auto-lock if balance < minimum
     */
    private void processOrderCompletion(Order order) {
        if (order.getTechnician() == null || order.getFinalPrice() == null || order.getFinalPrice() <= 0) {
            return;
        }

        User technician = order.getTechnician();
        BigDecimal finalPrice = BigDecimal.valueOf(order.getFinalPrice());

        // Get or create technician wallet
        Wallet wallet = walletRepository.findWithLockByUser_Id(technician.getId())
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .user(technician)
                        .currency("VND")
                        .build()));

        // Get fixed commission fee from system settings
        BigDecimal commissionFee = getFixedCommissionFee();

        // Record revenue addition transaction
        BigDecimal balanceAfterRevenue = wallet.getBalance().add(finalPrice);
        WalletTransaction revenueTransaction = WalletTransaction.builder()
                .transactionCode(transactionCodeGenerator.generateTransactionCode(TransactionType.COMMISSION))
                .wallet(wallet)
                .order(order)
                .type(TransactionType.COMMISSION)
                .category("REVENUE")
                .title("Thu nhập đơn hoàn thành")
                .amount(finalPrice)
                .fee(BigDecimal.ZERO)
                .netAmount(finalPrice)
                .afterBalance(balanceAfterRevenue.longValueExact())
                .note("Cộng " + finalPrice + " từ đơn " + order.getCode())
                .actor("SYSTEM")
                .status(TransactionStatus.SUCCESS)
                .processedAt(LocalDateTime.now())
                .build();
        walletTransactionRepository.save(revenueTransaction);

        // Update wallet balance with revenue
        wallet.setBalance(balanceAfterRevenue);
        wallet.setTotalEarned(wallet.getTotalEarned().add(finalPrice));

        // Record commission deduction transaction
        BigDecimal balanceAfterDeduction = balanceAfterRevenue.subtract(commissionFee);
        WalletTransaction commissionTransaction = WalletTransaction.builder()
                .transactionCode(transactionCodeGenerator.generateTransactionCode(TransactionType.COMMISSION))
                .wallet(wallet)
                .order(order)
                .type(TransactionType.COMMISSION)
                .category("COMMISSION_DEDUCTION")
                .title("Trừ phí hoa hồng")
                .amount(commissionFee.negate())
                .fee(BigDecimal.ZERO)
                .netAmount(commissionFee.negate())
                .afterBalance(Math.max(0, balanceAfterDeduction.longValueExact()))
                .note("Trừ phí hoa hồng cho đơn " + order.getCode())
                .actor("SYSTEM")
                .status(TransactionStatus.SUCCESS)
                .processedAt(LocalDateTime.now())
                .build();
        walletTransactionRepository.save(commissionTransaction);

        // Update wallet balance with commission deduction
        wallet.setBalance(balanceAfterDeduction.max(BigDecimal.ZERO));
        walletRepository.save(wallet);
    }

    private BigDecimal getFixedCommissionFee() {
        return systemSettingRepository.findByKey("fixed_commission_fee")
                .map(s -> {
                    try {
                        return new BigDecimal(s.getValue());
                    } catch (Exception ex) {
                        return new BigDecimal("10000");
                    }
                })
                .orElse(new BigDecimal("10000"));
    }

    private BigDecimal getMinimumCommissionBalance() {
        return systemSettingRepository.findByKey("minimum_commission_balance")
                .map(s -> {
                    try {
                        return new BigDecimal(s.getValue());
                    } catch (Exception ex) {
                        return new BigDecimal("0");
                    }
                })
                .orElse(new BigDecimal("0"));
    }

    @Transactional
    public PriceAdjustmentEnvelope requestPriceAdjustment(String code, PriceAdjustmentRequest request) {
        Order order = findOrder(code);
        User technician = getCurrentUser();

        if (technician.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể đề nghị điều chỉnh giá");
        }
        if (order.getTechnician() == null || !order.getTechnician().getId().equals(technician.getId())) {
            throw AppException.forbidden("Bạn không phải thợ phụ trách đơn này");
        }
        if (order.getStatus() != OrderStatus.IN_PROGRESS && order.getStatus() != OrderStatus.SCHEDULED) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Chỉ điều chỉnh giá khi đơn đang xử lý");
        }
        if (request.getNewPrice() == null || request.getNewPrice() <= 0) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Giá mới không hợp lệ");
        }

        // Reject any prior PENDING adjustment to keep at most one open at a time.
        adjustmentRepository.findTopByOrder_IdAndStatusOrderByRequestedAtDesc(
                order.getId(), PriceAdjustmentStatus.PENDING).ifPresent(prev -> {
                    prev.setStatus(PriceAdjustmentStatus.REJECTED);
                    prev.setRejectedAt(LocalDateTime.now());
                    prev.setRejectionReason("Bị thay thế bởi đề nghị mới");
                    adjustmentRepository.save(prev);
                });

        Long original = order.getEstimatedPrice() != null ? order.getEstimatedPrice() : 0L;

        OrderPriceAdjustment adjustment = OrderPriceAdjustment.builder()
                .order(order)
                .originalPrice(original)
                .newPrice(request.getNewPrice())
                .reason(request.getReason())
                .status(PriceAdjustmentStatus.PENDING)
                .build();

        if (request.getEvidenceImages() != null) {
            adjustment.getEvidenceImages().addAll(
                    request.getEvidenceImages().stream()
                            .filter(s -> s != null && !s.isBlank())
                            .map(String::trim)
                            .toList());
        }

        if (request.getParts() != null) {
            for (PriceAdjustmentRequest.PartItem p : request.getParts()) {
                adjustment.getParts().add(OrderPriceAdjustmentPart.builder()
                        .adjustment(adjustment)
                        .name(p.getName())
                        .price(p.getPrice())
                        .partCode(p.getPartCode())
                        .build());
            }
        }

        order.getPriceAdjustments().add(adjustment);
        orderRepository.save(order);

        eventPublisher.publishPriceAdjustmentRequested(
                order.getCode(), original, adjustment.getNewPrice(), adjustment.getReason());
        notifyOrderEvent(order.getCustomer(), NotificationType.PRICE_ADJUSTMENT,
                "Thợ điều chỉnh chi phí",
                "Đơn " + order.getCode() + " có phát sinh thêm "
                        + (adjustment.getNewPrice() - original) + "đ",
                order.getCode());

        return PriceAdjustmentEnvelope.builder()
                .id(order.getCode())
                .priceAdjustment(orderMapper.toAdjustmentResponse(adjustment))
                .build();
    }

    @Override
    @Transactional
    public PriceAdjustmentEnvelope approvePriceAdjustment(String code) {
        Order order = findOrder(code);
        User customer = getCurrentUser();
        ensureCustomerOf(order, customer);

        OrderPriceAdjustment adj = adjustmentRepository
                .findTopByOrder_IdAndStatusOrderByRequestedAtDesc(order.getId(), PriceAdjustmentStatus.PENDING)
                .orElseThrow(() -> AppException.notFound("Không có đề nghị điều chỉnh giá đang chờ duyệt"));

        adj.setStatus(PriceAdjustmentStatus.APPROVED);
        adj.setApprovedAt(LocalDateTime.now());

        // Sync the order's final price with the approved value.
        order.setFinalPrice(adj.getNewPrice());
        adjustmentRepository.save(adj);
        orderRepository.save(order);

        return PriceAdjustmentEnvelope.builder()
                .id(order.getCode())
                .priceAdjustment(orderMapper.toAdjustmentResponse(adj))
                .build();
    }

    @Override
    @Transactional
    public PriceAdjustmentEnvelope rejectPriceAdjustment(String code, RejectPriceAdjustmentRequest request) {
        Order order = findOrder(code);
        User customer = getCurrentUser();
        ensureCustomerOf(order, customer);

        OrderPriceAdjustment adj = adjustmentRepository
                .findTopByOrder_IdAndStatusOrderByRequestedAtDesc(order.getId(), PriceAdjustmentStatus.PENDING)
                .orElseThrow(() -> AppException.notFound("Không có đề nghị điều chỉnh giá đang chờ duyệt"));

        adj.setStatus(PriceAdjustmentStatus.REJECTED);
        adj.setRejectedAt(LocalDateTime.now());
        adj.setRejectionReason(request.getReason());
        adjustmentRepository.save(adj);

        return PriceAdjustmentEnvelope.builder()
                .id(order.getCode())
                .priceAdjustment(orderMapper.toAdjustmentResponse(adj))
                .build();
    }

    // ===============================================================
    // Authorization helpers
    // ===============================================================

    private void ensureCanRead(Order order, User user) {
        if (user.getRole() == Role.ADMIN) return;
        if (user.getRole() == Role.CUSTOMER
                && order.getCustomer() != null
                && order.getCustomer().getId().equals(user.getId())) {
            return;
        }
        if (user.getRole() == Role.TECHNICIAN
                && order.getTechnician() != null
                && order.getTechnician().getId().equals(user.getId())) {
            return;
        }
        throw AppException.forbidden("Bạn không có quyền xem đơn này");
    }

    private OrderActor ensureCanCancel(Order order, User user) {
        if (user.getRole() == Role.ADMIN) return OrderActor.ADMIN;
        if (user.getRole() == Role.CUSTOMER
                && order.getCustomer() != null
                && order.getCustomer().getId().equals(user.getId())) {
            return OrderActor.CUSTOMER;
        }
        if (user.getRole() == Role.TECHNICIAN
                && order.getTechnician() != null
                && order.getTechnician().getId().equals(user.getId())) {
            return OrderActor.TECHNICIAN;
        }
        throw AppException.forbidden("Bạn không có quyền hủy đơn này");
    }

    private void ensureCustomerOf(Order order, User user) {
        if (user.getRole() != Role.CUSTOMER
                || order.getCustomer() == null
                || !order.getCustomer().getId().equals(user.getId())) {
            throw AppException.forbidden("Chỉ khách hàng đặt đơn mới được thực hiện thao tác này");
        }
    }

    /** Allowed transitions for the technician's generic status updates. */
    private void ensureCanTransition(Order order, User current, OrderStatus target) {
        OrderStatus from = order.getStatus();

        if (current.getRole() == Role.ADMIN) return; // admin can do anything

        if (current.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể chuyển trạng thái đơn");
        }
        if (order.getTechnician() == null || !order.getTechnician().getId().equals(current.getId())) {
            throw AppException.forbidden("Bạn không phải thợ phụ trách đơn này");
        }

        boolean allowed = (from == OrderStatus.SCHEDULED && target == OrderStatus.IN_PROGRESS)
                || (from == OrderStatus.IN_PROGRESS && target == OrderStatus.COMPLETED);

        if (!allowed) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Chuyển trạng thái không hợp lệ: " + from + " -> " + target);
        }
    }

    // ===============================================================
    // Internal utilities
    // ===============================================================

    private Order findOrder(String code) {
        return orderRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn hàng " + code));
    }

    private void recordHistory(Order order, OrderStatus from, OrderStatus to,
                                OrderActor actor, Long actorUserId, String note) {
        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new ArrayList<>());
        }
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(from)
                .toStatus(to)
                .actor(actor)
                .actorUserId(actorUserId)
                .note(note)
                .build());
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

    /**
     * Persist a notification + push WS event for the recipient. Best-effort:
     * any failure is logged but never propagated, so the underlying business
     * transaction is never rolled back because of a notification glitch.
     */
    private void notifyOrderEvent(User recipient, NotificationType type,
                                   String title, String body, String orderCode) {
        if (recipient == null) return;
        try {
            notificationService.createNotification(
                    recipient, type, title, body, java.util.Map.of("orderId", orderCode));
        } catch (Exception ex) {
            log.warn("Failed to create notification for {} on order {}: {}",
                    recipient.getCode(), orderCode, ex.getMessage());
        }
    }

    // ===============================================================
    // Payment flow
    // ===============================================================

    @Override
    @Transactional
    public void createPaymentTransactionForOrder(Long orderId) {
        // 1. Find order by id and check status = COMPLETED
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng không tìm thấy"));
        
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION, 
                    "Đơn phải ở trạng thái hoàn thành mới có thể thanh toán");
        }
        
        // 2. Get customer wallet
        Wallet wallet = order.getCustomer().getWallet();
        if (wallet == null) {
            throw AppException.badRequest(ErrorCode.BAD_REQUEST, "Không tìm thấy ví khách hàng");
        }
        
        // 3. Create WalletTransaction with type=PAYMENT, status=SUCCESS
        BigDecimal finalPrice = BigDecimal.valueOf(order.getFinalPrice() != null ? order.getFinalPrice() : 0L);
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionCode(transactionCodeGenerator.generateTransactionCode(TransactionType.PAYMENT))
                .wallet(wallet)
                .type(TransactionType.PAYMENT)
                .category("PAYMENT")
                .title("Thanh toán đơn hàng " + order.getCode())
                .amount(finalPrice)
                .fee(BigDecimal.ZERO)
                .netAmount(finalPrice)
                .afterBalance(wallet.getBalance().add(finalPrice).longValueExact())
                .note("Thanh toán đơn hàng " + order.getCode())
                .actor("SYSTEM")
                .relatedOrderCode(order.getCode())
                .status(TransactionStatus.SUCCESS)
                .order(order)
                .processedAt(LocalDateTime.now())
                .build();
        
        walletTransactionRepository.save(transaction);
        
        // 4. Update wallet balance
        wallet.setBalance(wallet.getBalance().add(transaction.getNetAmount()));
        walletRepository.save(wallet);
        
        log.info("Payment transaction created for order {} with amount {}", order.getCode(), finalPrice);
    }
}
