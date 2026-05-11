package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.review.ReviewResponse;
import com.example.becommerce.entity.Review;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        if (review == null) return null;

        List<String> images = review.getAttachedImages();

        return ReviewResponse.builder()
                .id(review.getCode())
                .orderId(review.getOrder() == null ? null : review.getOrder().getCode())
                .authorName(review.getAuthor() == null ? null : review.getAuthor().getFullName())
                .authorAvatar(review.getAuthor() == null ? null : review.getAuthor().getAvatar())
                .rating(review.getRating())
                .content(review.getContent())
                .attachedImages(images != null && !images.isEmpty() ? images : null)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
