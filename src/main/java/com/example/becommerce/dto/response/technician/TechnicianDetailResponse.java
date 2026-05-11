package com.example.becommerce.dto.response.technician;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Full technician profile returned by GET /api/technicians/:id.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicianDetailResponse {

    private String id;
    private String fullName;
    private String avatar;
    private String coverImage;

    private String phone;
    private String email;

    private String location;
    private String district;
    private String bio;

    private List<String> skills;

    private BigDecimal rating;
    private Long reviewCount;
    private Long completedJobs;

    private Boolean isAvailable;
    private String type;
    private String titleBadge;
    private String verificationStatus;
    private Integer yearsExperience;

    private Map<String, String> schedule;

    private LocalDateTime joinedAt;
    private LocalDateTime lastActiveAt;
}
