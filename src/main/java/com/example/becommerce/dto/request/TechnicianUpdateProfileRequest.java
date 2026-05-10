package com.example.becommerce.dto.request;

import lombok.*;

import java.util.List;

/**
 * Request DTO for updating technician profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianUpdateProfileRequest {

    /**
     * Full name
     */
    private String fullName;

    /**
     * Phone number
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
     * Danh sách ID dịch vụ
     */
    private List<Long> serviceIds;

    /**
     * Danh sách khu vực làm việc
     */
    private List<String> workingAreas;

    /**
     * Giá cơ bản (VND)
     */
    private Long basePrice;

    /**
     * Giá theo giờ (VND)
     */
    private Long hourlyRate;
}
