package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.KycStatus;
import com.example.becommerce.entity.enums.TechnicianStatus;
import lombok.*;

/**
 * Response DTO for technician availability update
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianAvailabilityResponse {

    /**
     * ID của TechnicianProfile
     */
    private Long technicianId;

    /**
     * Trạng thái sẵn sàng nhận đơn
     */
    private Boolean available;

    /**
     * Trạng thái xác minh danh tính
     */
    private KycStatus kycStatus;

    /**
     * Trạng thái đăng ký thợ
     */
    private TechnicianStatus technicianStatus;

    /**
     * Message
     */
    private String message;
}
