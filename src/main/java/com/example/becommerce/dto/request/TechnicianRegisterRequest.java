package com.example.becommerce.dto.request;

import lombok.*;

import java.util.List;

/**
 * Request DTO for registering a new technician.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianRegisterRequest {

    /**
     * Số năm kinh nghiệm
     */
    private Integer experienceYears;

    /**
     * Mô tả ngắn về thợ
     */
    private String bio;

    /**
     * Danh sách ID dịch vụ
     */
    private List<Long> serviceIds;

    /**
     * Danh sách khu vực làm việc (format: "HN:Hà Đông", "HN:Ba Đình", etc.)
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
