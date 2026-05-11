package com.example.becommerce.dto.response.verification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Full verification detail returned by GET /api/verifications/:id.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDetailResponse {

    private String id;
    private String technicianId;
    private String fullName;
    private String phone;
    private String email;
    private String district;
    private String serviceCategory;
    private Integer yearsExperience;

    private String status;
    private String note;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime submittedAt;

    private VerificationDocumentsView documents;
}
