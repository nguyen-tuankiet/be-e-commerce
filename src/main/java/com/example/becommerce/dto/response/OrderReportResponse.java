package com.example.becommerce.dto.response;

import com.example.becommerce.entity.enums.ReportReason;
import com.example.becommerce.entity.enums.ReportStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho order report
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReportResponse {
    private Long reportId;
    private String code;
    private Long orderId;
    private Long reporterId;
    private Long againstId;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private String resolutionNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long resolvedBy;
    private List<String> evidenceUrls;
}
