package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.WarrantyMapper;
import com.example.becommerce.dto.request.warranty.CreateWarrantyRequest;
import com.example.becommerce.dto.response.warranty.WarrantyResponse;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.WarrantyClaim;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.entity.enums.WarrantyStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.repository.WarrantyClaimRepository;
import com.example.becommerce.service.WarrantyService;
import com.example.becommerce.service.WsEventPublisher;
import com.example.becommerce.utils.WarrantyCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Warranty business rules:
 *  - A claim can be filed by the customer of the order, only after the order is COMPLETED.
 *  - warrantyExpiresAt = order.completedAt + (order.warrantyMonths OR default 3 months).
 *  - One claim per order at a time; new claim replaces the prior pending one.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarrantyServiceImpl implements WarrantyService {

    private static final int DEFAULT_WARRANTY_MONTHS = 3;

    private final WarrantyClaimRepository warrantyRepository;
    private final OrderRepository         orderRepository;
    private final UserRepository          userRepository;
    private final WarrantyMapper          warrantyMapper;
    private final WarrantyCodeGenerator   codeGenerator;
    private final WsEventPublisher eventPublisher;

    @Override
    @Transactional
    public WarrantyResponse createWarranty(String orderCode, CreateWarrantyRequest request) {
        User customer = getCurrentUser();
        Order order = findOrder(orderCode);
        ensureCustomerOf(order, customer);

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Chỉ có thể yêu cầu bảo hành đơn đã hoàn thành");
        }

        LocalDateTime expiresAt = computeExpiry(order);
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw AppException.badRequest(ErrorCode.WARRANTY_EXPIRED, "Thời hạn bảo hành đã hết");
        }

        WarrantyClaim claim = WarrantyClaim.builder()
                .code(codeGenerator.generate())
                .order(order)
                .customer(customer)
                .technician(order.getTechnician())
                .description(request.getDescription())
                .status(WarrantyStatus.PENDING)
                .scheduledAt(request.getScheduledAt())
                .warrantyExpiresAt(expiresAt)
                .build();

        if (request.getImages() != null) {
            request.getImages().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .forEach(claim.getImages()::add);
        }

        WarrantyClaim saved = warrantyRepository.save(claim);
        log.info("Warranty {} created on order {} by customer {}", saved.getCode(), order.getCode(), customer.getCode());

        eventPublisher.publishOrderStatusChanged(
                order.getCode(),
                order.getStatus().apiValue(),
                order.getStatus().apiValue()
        );

        return warrantyMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyResponse getWarrantyByOrder(String orderCode) {
        Order order = findOrder(orderCode);
        User current = getCurrentUser();
        ensureReadable(order, current);

        WarrantyClaim claim = warrantyRepository.findTopByOrder_IdOrderByCreatedAtDesc(order.getId())
                .orElseThrow(() -> AppException.notFound("Đơn này chưa có yêu cầu bảo hành"));

        return warrantyMapper.toResponse(claim);
    }

    // ----------------------------------------------------------------

    private LocalDateTime computeExpiry(Order order) {
        if (order.getCompletedAt() == null) return null;
        int months = order.getWarrantyMonths() != null ? order.getWarrantyMonths() : DEFAULT_WARRANTY_MONTHS;
        return order.getCompletedAt().plusMonths(months);
    }

    private Order findOrder(String code) {
        return orderRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn hàng " + code));
    }

    private void ensureCustomerOf(Order order, User user) {
        if (user.getRole() != Role.CUSTOMER
                || order.getCustomer() == null
                || !order.getCustomer().getId().equals(user.getId())) {
            throw AppException.forbidden("Chỉ khách hàng đặt đơn mới được yêu cầu bảo hành");
        }
    }

    private void ensureReadable(Order order, User user) {
        if (user.getRole() == Role.ADMIN) return;
        boolean isCustomerOwner = user.getRole() == Role.CUSTOMER
                && order.getCustomer() != null
                && order.getCustomer().getId().equals(user.getId());
        boolean isTechnician = user.getRole() == Role.TECHNICIAN
                && order.getTechnician() != null
                && order.getTechnician().getId().equals(user.getId());
        if (!isCustomerOwner && !isTechnician) {
            throw AppException.forbidden("Bạn không có quyền xem bảo hành đơn này");
        }
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

    @Override
    @Transactional
    public WarrantyResponse updateWarrantyStatus(String warrantyCode, String newStatus) {
        User technician = getCurrentUser();

        WarrantyClaim claim = warrantyRepository.findByCode(warrantyCode)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy yêu cầu bảo hành"));

        // Check quyền: Chỉ thợ của đơn này mới được duyệt
        if (technician.getRole() != Role.TECHNICIAN || !claim.getTechnician().getId().equals(technician.getId())) {
            throw AppException.forbidden("Bạn không có quyền xử lý yêu cầu bảo hành này");
        }

        if (claim.getStatus() != WarrantyStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS, "Yêu cầu này đã được xử lý");
        }

        Order originalOrder = claim.getOrder();
        Order warrantyOrder = null;

        if ("in_progress".equalsIgnoreCase(newStatus)) {
            claim.setStatus(WarrantyStatus.IN_PROGRESS);

            warrantyOrder = Order.builder()
                    .code(codeGenerator.generate())
                    .customer(claim.getCustomer())
                    .technician(technician)
                    .deviceName(originalOrder.getDeviceName())
                    .description("BẢO HÀNH: " + claim.getDescription())
                    .address(originalOrder.getAddress())
                    .serviceCategory(originalOrder.getServiceCategory())
                    .serviceName(originalOrder.getServiceName())
                    .subService(originalOrder.getSubService())
                    .estimatedPrice(0L) // Giá 0đ
                    .finalPrice(0L)     // Giá 0đ
                    .scheduledAt(claim.getScheduledAt())
                    .status(OrderStatus.SCHEDULED) // Ném thẳng vào tab Sắp hẹn
                    .isWarranty(true) // Đánh cờ đây là đơn bảo hành (để FE hiện Tag màu cam)
                    .build();

            orderRepository.save(warrantyOrder);
            log.info("Auto-generated 0 VND Warranty Order {} from Claim {}", warrantyOrder.getCode(), claim.getCode());

        } else if ("rejected".equalsIgnoreCase(newStatus)) {
            claim.setStatus(WarrantyStatus.REJECTED);
        } else {
            throw AppException.badRequest(ErrorCode.INVALID_STATUS, "Trạng thái không hợp lệ");
        }

        WarrantyClaim saved = warrantyRepository.save(claim);

        // BỔ SUNG: Bắn event 1 -> Báo cho đơn gốc biết trạng thái bảo hành đã bị đổi (để khách hàng thấy box Xanh/Đỏ)
        eventPublisher.publishOrderStatusChanged(
                originalOrder.getCode(),
                originalOrder.getStatus().apiValue(),
                originalOrder.getStatus().apiValue()
        );

        // BỔ SUNG: Bắn event 2 -> Nếu có sinh ra đơn 0đ, bắn event cho đơn mới để nó tự lọt vào tab Bảo hành / Sắp hẹn
        if (warrantyOrder != null) {
            eventPublisher.publishOrderStatusChanged(
                    warrantyOrder.getCode(),
                    null,
                    OrderStatus.SCHEDULED.apiValue()
            );
        }

        return warrantyMapper.toResponse(saved);
    }
}
