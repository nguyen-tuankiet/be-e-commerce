package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.ReviewResponse;
import com.example.becommerce.entity.Review;
import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review, String customerName, String technicianName) {
        if (review == null) return null;
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .customerId(review.getCustomerId())
                .customerName(customerName)
                .technicianId(review.getTechnicianId())
                .technicianName(technicianName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
