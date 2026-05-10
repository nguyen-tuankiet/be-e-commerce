package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.KycStatus;
import com.example.becommerce.entity.enums.TechnicianStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for technician registration result.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianRegisterResponse {

    /**
     * ID của TechnicianProfile vừa tạo
     */
    private Long technicianProfileId;

    /**
     * Trạng thái đăng ký thợ
     */
    private TechnicianStatus technicianStatus;

    /**
     * Trạng thái xác minh danh tính
     */
    private KycStatus kycStatus;

    /**
     * Bước tiếp theo (UPLOAD_KYC_DOCUMENTS)
     */
    private String nextStep;
}
