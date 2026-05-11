package com.example.becommerce.dto.response.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for POST /api/orders/:id/reviews.
 * Mirrors the structure used in the technician reviews list as well.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private String id;
    private String orderId;

    private String authorName;
    private String authorAvatar;

    private Integer rating;
    private String content;
    private List<String> attachedImages;

    private LocalDateTime createdAt;
}
