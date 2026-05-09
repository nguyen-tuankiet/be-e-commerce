package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.ReviewMapper;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.ReviewResponse;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.Review;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.ReviewRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.ReviewService;
import com.example.becommerce.utils.ReviewSpecification;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(Long orderId, Integer rating, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn hàng với id: " + orderId));

        User customer = getCurrentUser();
        if (customer.getRole() != Role.CUSTOMER) {
            throw AppException.forbidden("Chỉ khách hàng mới có thể đánh giá");
        }
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw AppException.forbidden("Bạn không phải chủ của đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Chỉ được đánh giá đơn hàng đã hoàn thành");
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Đơn hàng đã được đánh giá");
        }
        if (order.getTechnician() == null) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Đơn hàng chưa có thợ phụ trách");
        }

        Review review = Review.builder()
                .orderId(orderId)
                .customerId(customer.getId())
                .technicianId(order.getTechnician().getId())
                .rating(rating)
                .comment(comment)
                .build();
        review = reviewRepository.save(review);

        Double avg = reviewRepository.averageRatingByTechnicianId(order.getTechnician().getId());
        User technician = order.getTechnician();
        technician.setAverageRating(avg);
        userRepository.save(technician);

        log.info("Review created for order [{}] by customer [{}], rating={}",
                order.getCode(), customer.getCode(), rating);

        return reviewMapper.toResponse(review, customer.getFullName(), technician.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviews(Integer rating, Long technicianId, int page, int limit) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw AppException.forbidden("Chỉ quản trị viên mới có thể xem danh sách đánh giá");
        }

        Specification<Review> spec = ReviewSpecification.buildFilter(rating, technicianId);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);

        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);

        List<ReviewResponse> items = reviewPage.getContent().stream()
                .map(r -> {
                    String customerName = userRepository.findById(r.getCustomerId())
                            .map(User::getFullName).orElse(null);
                    String technicianName = userRepository.findById(r.getTechnicianId())
                            .map(User::getFullName).orElse(null);
                    return reviewMapper.toResponse(r, customerName, technicianName);
                })
                .toList();

        return PagedResponse.of(items, page, limit, reviewPage.getTotalElements());
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
