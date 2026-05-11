package com.example.becommerce.dto.response.technician;

import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.review.ReviewResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregate header + paged items returned by GET /api/technicians/:id/reviews.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicianReviewListResponse {

    private BigDecimal averageRating;
    private Long totalReviews;

    private List<ReviewResponse> items;
    private PagedResponse.PaginationMeta pagination;
}
