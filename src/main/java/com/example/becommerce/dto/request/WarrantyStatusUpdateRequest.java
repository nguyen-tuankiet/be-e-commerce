package com.example.becommerce.dto.request;

import com.example.becommerce.entity.enums.WarrantyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO để cập nhật warranty claim status
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private WarrantyStatus status;

    private String note;
}
