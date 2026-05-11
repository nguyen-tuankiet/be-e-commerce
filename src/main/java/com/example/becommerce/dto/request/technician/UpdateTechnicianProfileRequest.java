package com.example.becommerce.dto.request.technician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * All fields optional — only non-null values are applied (PATCH semantics).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTechnicianProfileRequest {

    private String fullName;
    private String phone;
    private String email;
    private String avatar;
    private String coverImage;
    private String bio;

    private String district;
    private String serviceCategory;

    private List<String> skills;
    private List<String> areas;

    private Long pricePerHour;
    private Integer yearsExperience;

    /** Day name -> "HH:mm-HH:mm" or null to clear that day. */
    private Map<String, String> schedule;
}
