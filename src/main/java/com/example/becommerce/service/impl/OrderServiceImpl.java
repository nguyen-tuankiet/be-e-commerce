package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.OrderMapper;
import com.example.becommerce.dto.request.OrderCancelRequest;
import com.example.becommerce.dto.request.OrderCreateRequest;
import com.example.becommerce.dto.request.OrderSearchRequest;
import com.example.becommerce.dto.response.OrderResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.OrderService;
import com.example.becommerce.utils.OrderSpecification;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    private static final String ORDER_CODE_PREFIX = "ORD-";

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        User customer = getCurrentUser();

        if (customer.getRole() != Role.CUSTOMER) {
            throw AppException.forbidden("Chỉ khách hàng mới có thể tạo đơn hàng");
        }

        long count = orderRepository.countAll();
        String code = ORDER_CODE_PREFIX + String.format("%04d", count + 1);

        Order order = Order.builder()
                .code(code)
                .customer(customer)
                .serviceName(request.getServiceName())
                .description(request.getDescription())
                .address(request.getAddress())
                .district(request.getDistrict())
                .totalPrice(request.getTotalPrice())
                .note(request.getNote())
                .build();

        order = orderRepository.save(order);
        log.info("Order created: {} by customer [{}]", order.getCode(), customer.getCode());

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn hàng với id: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrders(OrderSearchRequest request) {
        Specification<Order> spec = OrderSpecification.buildFilter(request);

        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                request.getLimit(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> items = orderPage.getContent()
                .stream()
                .map(orderMapper::toResponse)
                .toList();

        return PagedResponse.of(items, request.getPage(), request.getLimit(), orderPage.getTotalElements());
    }

    @Override
    @Transactional
    public OrderResponse acceptOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Đơn hàng phải ở trạng thái PENDING mới có thể nhận");
        }
        if (order.getTechnician() != null) {
            throw AppException.badRequest(ErrorCode.ORDER_ALREADY_ASSIGNED,
                    "Đơn hàng này đã có thợ nhận");
        }

        User technician = getCurrentUser();
        if (technician.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể nhận đơn");
        }

        order.setTechnician(technician);
        order.setStatus(OrderStatus.ACCEPTED);
        order = orderRepository.save(order);
        log.info("Order [{}] accepted by technician [{}]", order.getCode(), technician.getCode());

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse startOrder(Long orderId) {
        Order order = findOrderById(orderId);
        User technician = getCurrentUser();

        verifyTechnicianOwnsOrder(order, technician);

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Đơn hàng phải ở trạng thái ACCEPTED mới có thể bắt đầu thực hiện");
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        order = orderRepository.save(order);
        log.info("Order [{}] started by technician [{}]", order.getCode(), technician.getCode());

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        Order order = findOrderById(orderId);
        User technician = getCurrentUser();

        verifyTechnicianOwnsOrder(order, technician);

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Đơn hàng phải ở trạng thái IN_PROGRESS mới có thể hoàn thành");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepository.save(order);
        log.info("Order [{}] completed by technician [{}]", order.getCode(), technician.getCode());

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String cancelReason) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.ACCEPTED) {
            throw AppException.badRequest(ErrorCode.ORDER_CANNOT_CANCEL,
                    "Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc ACCEPTED");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);
        order = orderRepository.save(order);
        log.info("Order [{}] cancelled. Reason: {}", order.getCode(), cancelReason);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse rejectOrder(Long orderId) {
        Order order = findOrderById(orderId);
        User technician = getCurrentUser();

        verifyTechnicianOwnsOrder(order, technician);

        order.setTechnician(null);
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);
        log.info("Order [{}] rejected by technician [{}], returned to PENDING",
                order.getCode(), technician.getCode());

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse reportExtraCost(Long orderId, BigDecimal extraCost, String reason) {
        Order order = findOrderById(orderId);
        User technician = getCurrentUser();
        verifyTechnicianOwnsOrder(order, technician);

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Chỉ có thể báo chi phí phát sinh khi đơn đang ở trạng thái IN_PROGRESS");
        }

        order.setExtraCost(extraCost);
        order.setExtraCostReason(reason);
        order.setStatus(OrderStatus.WAITING_FOR_PRICE_APPROVAL);
        order = orderRepository.save(order);
        log.info("Order [{}]: extra cost {} reported by technician [{}]. Reason: {}",
                order.getCode(), extraCost, technician.getCode(), reason);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse approveExtraCost(Long orderId) {
        Order order = findOrderById(orderId);
        User customer = getCurrentUser();
        verifyCustomerOwnsOrder(order, customer);

        if (order.getStatus() != OrderStatus.WAITING_FOR_PRICE_APPROVAL) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Không có yêu cầu phê duyệt chi phí phát sinh nào đang chờ");
        }
        if (order.getExtraCost() == null || order.getExtraCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Không có chi phí phát sinh để phê duyệt");
        }

        BigDecimal newTotal = order.getTotalPrice().add(order.getExtraCost());
        order.setTotalPrice(newTotal);
        order.setExtraCost(null);
        order.setExtraCostReason(null);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order = orderRepository.save(order);
        log.info("Order [{}]: customer approved extra cost, new total = {}",
                order.getCode(), newTotal);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse rejectExtraCost(Long orderId) {
        Order order = findOrderById(orderId);
        User customer = getCurrentUser();
        verifyCustomerOwnsOrder(order, customer);

        if (order.getStatus() != OrderStatus.WAITING_FOR_PRICE_APPROVAL) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Không có yêu cầu phê duyệt chi phí phát sinh nào đang chờ");
        }

        order.setExtraCost(null);
        order.setExtraCostReason(null);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order = orderRepository.save(order);
        log.info("Order [{}]: customer rejected extra cost, continuing with original price",
                order.getCode());

        return orderMapper.toResponse(order);
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn hàng với id: " + id));
    }

    private void verifyTechnicianOwnsOrder(Order order, User technician) {
        if (order.getTechnician() == null
                || !order.getTechnician().getId().equals(technician.getId())) {
            throw AppException.badRequest(ErrorCode.ORDER_NOT_OWN_TECHNICIAN,
                    "Bạn không phải thợ phụ trách đơn hàng này");
        }
    }

    private void verifyCustomerOwnsOrder(Order order, User customer) {
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw AppException.badRequest(ErrorCode.ORDER_NOT_OWN_CUSTOMER,
                    "Bạn không phải chủ của đơn hàng này");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw AppException.unauthorized("Chưa xác thực người dùng");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        throw AppException.unauthorized("Không thể xác thực người dùng");
    }
}
