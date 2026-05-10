package com.example.becommerce.dto.request;

import com.example.becommerce.entity.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO để admin chấp thuận/từ chối verification
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationUpdateStatusRequest {
    @NotNull(message = "Status is required")
    private KycStatus status;

    private String note;
}
