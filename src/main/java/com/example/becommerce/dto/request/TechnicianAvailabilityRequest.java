package com.example.becommerce.dto.request;

import lombok.*;

/**
 * Request DTO for updating technician availability.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianAvailabilityRequest {

    /**
     * Trạng thái sẵn sàng nhận đơn
     */
    private Boolean available;
}
