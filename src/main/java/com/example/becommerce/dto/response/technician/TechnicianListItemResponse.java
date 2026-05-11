package com.example.becommerce.dto.response.technician;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight technician summary used by GET /api/technicians.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicianListItemResponse {

    private String id;
    private String fullName;
    private String avatar;

    private BigDecimal rating;
    private Long reviewCount;

    private String location;
    private String district;

    private List<String> skills;

    private Long pricePerHour;

    private Boolean isAvailable;
    private String timeAvailable;

    private String type;
    private String titleBadge;

    private Long completedJobs;
}
