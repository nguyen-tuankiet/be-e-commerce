package com.example.becommerce.service;

import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.ReviewResponse;

public interface ReviewService {

    ReviewResponse createReview(Long orderId, Integer rating, String comment);

    PagedResponse<ReviewResponse> getReviews(Integer rating, Long technicianId, int page, int limit);
}
