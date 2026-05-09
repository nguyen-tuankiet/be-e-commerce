package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {

    private Long id;
    private Long orderId;
    private Long customerId;
    private String customerName;
    private Long technicianId;
    private String technicianName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
