package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.ReviewMapper;
import com.example.becommerce.dto.request.review.CreateReviewRequest;
import com.example.becommerce.dto.response.review.ReviewResponse;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.Review;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.ReviewRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.service.ReviewService;
import com.example.becommerce.utils.ReviewCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business rules for customer reviews:
 *  - Only the customer that placed the order can review it.
 *  - The order must be COMPLETED.
 *  - One review per order.
 *  - The technician on the order is the one being rated.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository    reviewRepository;
    private final OrderRepository     orderRepository;
    private final UserRepository      userRepository;
    private final ReviewMapper        reviewMapper;
    private final ReviewCodeGenerator codeGenerator;

    @Override
    @Transactional
    public ReviewResponse createReview(String orderCode, CreateReviewRequest request) {
        User customer = getCurrentUser();
        Order order = findOrder(orderCode);

        if (customer.getRole() != Role.CUSTOMER) {
            throw AppException.forbidden("Chỉ khách hàng mới có thể đánh giá đơn");
        }
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customer.getId())) {
            throw AppException.forbidden("Bạn không phải khách hàng của đơn này");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw AppException.badRequest(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Chỉ có thể đánh giá đơn đã hoàn thành");
        }
        if (order.getTechnician() == null) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR,
                    "Đơn chưa có thợ thực hiện nên không thể đánh giá");
        }
        if (reviewRepository.existsByOrder_Id(order.getId())) {
            throw AppException.conflict(ErrorCode.REVIEW_ALREADY_EXISTS, "Đơn này đã được đánh giá");
        }

        Review review = Review.builder()
                .code(codeGenerator.generate())
                .order(order)
                .author(customer)
                .technician(order.getTechnician())
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        if (request.getAttachedImages() != null) {
            request.getAttachedImages().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .forEach(review.getAttachedImages()::add);
        }

        Review saved = reviewRepository.save(review);
        log.info("Review {} created on order {} by customer {}", saved.getCode(), order.getCode(), customer.getCode());
        return reviewMapper.toResponse(saved);
    }

    // ----------------------------------------------------------------

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
