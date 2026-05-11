package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.ReportMapper;
import com.example.becommerce.dto.request.report.CreateReportRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.report.ReportResponse;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.OrderReport;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.ReportReason;
import com.example.becommerce.entity.enums.ReportStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderReportRepository;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.service.ReportService;
import com.example.becommerce.utils.OrderReportSpecification;
import com.example.becommerce.utils.ReportCodeGenerator;
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

import java.util.List;

/**
 * Report business rules:
 *  - Any participant of the order (customer/technician) can file a report.
 *  - Reports are reviewed by admins via the listing endpoint.
 *  - Each new report is created with status OPEN.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final OrderReportRepository reportRepository;
    private final OrderRepository       orderRepository;
    private final UserRepository        userRepository;
    private final ReportMapper          reportMapper;
    private final ReportCodeGenerator   codeGenerator;

    @Override
    @Transactional
    public ReportResponse createReport(String orderCode, CreateReportRequest request) {
        User current = getCurrentUser();
        Order order = findOrder(orderCode);
        ensureCanReport(order, current);

        ReportReason reason;
        try {
            reason = ReportReason.from(request.getReason());
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Lý do report không hợp lệ");
        }
        if (reason == null) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Lý do report không hợp lệ");
        }

        OrderReport report = OrderReport.builder()
                .code(codeGenerator.generate())
                .order(order)
                .customer(order.getCustomer())
                .technician(order.getTechnician())
                .reason(reason)
                .description(request.getDescription())
                .status(ReportStatus.OPEN)
                .build();

        if (request.getEvidenceImages() != null) {
            request.getEvidenceImages().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .forEach(report.getEvidenceImages()::add);
        }

        OrderReport saved = reportRepository.save(report);
        log.info("Report {} filed on order {} by {}", saved.getCode(), order.getCode(), current.getCode());
        return reportMapper.toResponse(saved, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReportResponse> listReports(String status, String keyword, int page, int limit) {
        Specification<OrderReport> spec = OrderReportSpecification.buildFilter(status, keyword);

        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<OrderReport> reportPage = reportRepository.findAll(spec, pageable);

        List<ReportResponse> items = reportPage.getContent().stream()
                .map(r -> reportMapper.toResponse(r, true))
                .toList();

        return PagedResponse.of(items, page, limit, reportPage.getTotalElements());
    }

    // ----------------------------------------------------------------

    private void ensureCanReport(Order order, User user) {
        if (user.getRole() == Role.ADMIN) return;

        boolean isCustomer = user.getRole() == Role.CUSTOMER
                && order.getCustomer() != null
                && order.getCustomer().getId().equals(user.getId());
        boolean isTechnician = user.getRole() == Role.TECHNICIAN
                && order.getTechnician() != null
                && order.getTechnician().getId().equals(user.getId());

        if (!isCustomer && !isTechnician) {
            throw AppException.forbidden("Bạn không có quyền report đơn này");
        }
    }

    private Order findOrder(String code) {
        return orderRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn hàng " + code));
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
