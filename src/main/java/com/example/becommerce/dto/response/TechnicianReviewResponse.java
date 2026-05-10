package com.example.becommerce.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for technician review
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianReviewResponse {

    /**
     * ID của review
     */
    private Long reviewId;

    /**
     * Tên khách hàng
     */
    private String customerName;

    /**
     * Avatar khách hàng
     */
    private String customerAvatar;

    /**
     * Đánh giá (1-5 sao)
     */
    private Integer rating;

    /**
     * Nội dung đánh giá
     */
    private String comment;

    /**
     * Danh sách ảnh
     */
    private java.util.List<String> images;

    /**
     * ID của booking
     */
    private Long bookingId;

    /**
     * Ngày tạo
     */
    private LocalDateTime createdAt;
}
