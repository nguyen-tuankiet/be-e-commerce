package com.example.becommerce.dto.request;

import com.example.becommerce.entity.enums.DocType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO để upload tài liệu KYC
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadVerificationDocumentRequest {
    @NotNull(message = "Document type is required")
    private DocType docType;

    @NotNull(message = "Document URL is required")
    private String documentUrl;
}
