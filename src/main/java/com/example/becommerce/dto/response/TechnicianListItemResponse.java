package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.KycStatus;
import com.example.becommerce.entity.enums.TechnicianStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for technician list (public profile)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianListItemResponse {

    /**
     * ID của TechnicianProfile
     */
    private Long technicianId;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Full name
     */
    private String fullName;

    /**
     * Avatar URL
     */
    private String avatarUrl;

    /**
     * Mô tả
     */
    private String bio;

    /**
     * Danh sách dịch vụ (tên dịch vụ)
     */
    private List<String> services;

    /**
     * Đánh giá trung bình
     */
    private BigDecimal ratingAverage;

    /**
     * Số đánh giá
     */
    private Integer reviewCount;

    /**
     * Số công việc hoàn thành
     */
    private Integer completedJobCount;

    /**
     * Giá cơ bản (VND)
     */
    private Long basePrice;

    /**
     * Giá theo giờ (VND)
     */
    private Long hourlyRate;

    /**
     * Trạng thái sẵn sàng nhận đơn
     */
    private Boolean available;

    /**
     * Danh sách khu vực làm việc
     */
    private List<String> workingAreas;
}
