package com.example.becommerce.dto.request;

import com.example.becommerce.entity.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO để admin cập nhật report status
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    private String resolutionNote;
}
