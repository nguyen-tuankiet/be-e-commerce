package com.example.becommerce.dto.response.verification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Item in the admin paged listing GET /api/verifications.
 * Includes the documents block as the spec sample shows them inline.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationListItemResponse {

    private String id;
    private String technicianId;
    private String fullName;
    private String phone;
    private String email;
    private String district;
    private String serviceCategory;
    private Integer yearsExperience;
    private String status;
    private LocalDateTime submittedAt;

    private VerificationDocumentsView documents;
}
