package com.example.becommerce.dto.response.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Single-report payload — used both for POST /orders/:id/reports
 * and for items inside the admin GET /api/reports listing.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    private String id;
    private String orderId;
    private String reason;
    private String description;
    private String status;

    private List<String> evidenceImages;

    private PartyRef customer;
    private PartyRef technician;

    private LocalDateTime createdAt;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PartyRef {
        private String id;
        private String fullName;
    }
}
