package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.report.ReportResponse;
import com.example.becommerce.entity.OrderReport;
import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportMapper {

    /**
     * Compact representation used both for the create response and inside
     * the admin paged listing.
     *
     * @param includeParties when true, embed customer/technician refs (admin list);
     *                       when false, return only the lightweight payload (create response).
     */
    public ReportResponse toResponse(OrderReport report, boolean includeParties) {
        if (report == null) return null;

        List<String> evidence = report.getEvidenceImages();

        return ReportResponse.builder()
                .id(report.getCode())
                .orderId(report.getOrder() == null ? null : report.getOrder().getCode())
                .reason(report.getReason() == null ? null : report.getReason().apiValue())
                .description(report.getDescription())
                .status(report.getStatus() == null ? null : report.getStatus().apiValue())
                .evidenceImages(includeParties && evidence != null && !evidence.isEmpty() ? evidence : null)
                .customer(includeParties ? toPartyRef(report.getCustomer()) : null)
                .technician(includeParties ? toPartyRef(report.getTechnician()) : null)
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportResponse.PartyRef toPartyRef(User user) {
        if (user == null) return null;
        return ReportResponse.PartyRef.builder()
                .id(user.getCode())
                .fullName(user.getFullName())
                .build();
    }
}
