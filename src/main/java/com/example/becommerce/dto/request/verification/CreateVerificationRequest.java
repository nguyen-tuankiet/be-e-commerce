package com.example.becommerce.dto.request.verification;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multipart form for technician KYC submission.
 * Files are stored via FileStorageService and only URLs persisted.
 */
@Getter
@Setter
public class CreateVerificationRequest {

    /** Optional. Defaults to the current user's code if not provided. */
    private String technicianId;

    @NotBlank(message = "Khu vực không được trống")
    private String district;

    @NotBlank(message = "Loại dịch vụ không được trống")
    private String serviceCategory;

    private Integer yearsExperience;

    private MultipartFile idFront;
    private MultipartFile idBack;
    private MultipartFile portrait;
    private MultipartFile certificate;
}
