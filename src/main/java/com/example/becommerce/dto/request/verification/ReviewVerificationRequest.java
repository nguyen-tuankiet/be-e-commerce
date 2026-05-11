package com.example.becommerce.dto.request.verification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin decision payload for PATCH /api/verifications/:id.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVerificationRequest {

    /** "approved" or "rejected". */
    @NotBlank(message = "Trạng thái không được trống")
    private String status;

    private String note;

    private String reviewedBy;

    @Builder.Default
    private Boolean notifyTechnician = false;
}
