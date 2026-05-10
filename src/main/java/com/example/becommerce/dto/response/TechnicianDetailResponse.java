package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.KycStatus;
import com.example.becommerce.entity.enums.TechnicianStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for technician profile detail
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianDetailResponse {

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
     * Email
     */
    private String email;

    /**
     * Phone
     */
    private String phone;

    /**
     * Avatar URL
     */
    private String avatarUrl;

    /**
     * Mô tả
     */
    private String bio;

    /**
     * Số năm kinh nghiệm
     */
    private Integer experienceYears;

    /**
     * Danh sách dịch vụ
     */
    private List<String> services;

    /**
     * Danh sách khu vực làm việc
     */
    private List<String> workingAreas;

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
     * Trạng thái đăng ký thợ
     */
    private TechnicianStatus technicianStatus;

    /**
     * Trạng thái xác minh danh tính
     */
    private KycStatus kycStatus;

    /**
     * Ngày tạo
     */
    private LocalDateTime createdAt;
}
