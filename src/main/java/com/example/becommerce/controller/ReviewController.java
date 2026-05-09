package com.example.becommerce.controller;

import com.example.becommerce.dto.request.CreateReviewRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.ReviewResponse;
import com.example.becommerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/orders/{id}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long id,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse data = reviewService.createReview(id, request.getRating(), request.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @GetMapping("/api/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviews(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        PagedResponse<ReviewResponse> data = reviewService.getReviews(rating, technicianId, page, limit);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
