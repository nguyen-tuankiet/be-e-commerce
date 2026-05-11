package com.example.becommerce.dto.response.verification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response for PATCH /api/verifications/:id (admin approve/reject).
 * Echoes the new state and includes the synced technician verification status.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationReviewResponse {

    private String id;
    private String status;
    private String note;
    private String reviewedBy;
    private LocalDateTime reviewedAt;

    private String technicianStatus;
}
