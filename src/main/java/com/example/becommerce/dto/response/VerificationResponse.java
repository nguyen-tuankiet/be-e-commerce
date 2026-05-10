package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.KycStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho verification record
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResponse {
    private Long verificationId;
    private String code;
    private Long technicianId;
    private String fullName;
    private String phone;
    private String email;
    private String district;
    private String city;
    private Integer yearsExperience;
    private KycStatus status;
    private String note;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private List<VerificationDocumentResponse> documents;
}
