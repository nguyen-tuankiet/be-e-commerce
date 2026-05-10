package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.DocType;
import lombok.*;

/**
 * Response DTO cho tài liệu verification
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationDocumentResponse {
    private Long documentId;
    private DocType docType;
    private String url;
}
