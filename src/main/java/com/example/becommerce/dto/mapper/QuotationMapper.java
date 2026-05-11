package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.chat.EmbeddedQuotation;
import com.example.becommerce.dto.response.quotation.AcceptQuotationResponse;
import com.example.becommerce.dto.response.quotation.QuotationResponse;
import com.example.becommerce.entity.Quotation;
import org.springframework.stereotype.Component;

@Component
public class QuotationMapper {

    public QuotationResponse toResponse(Quotation q) {
        if (q == null) return null;
        return QuotationResponse.builder()
                .id(q.getCode())
                .conversationId(q.getConversation() == null ? null : q.getConversation().getCode())
                .technicianId(q.getTechnician() == null ? null : q.getTechnician().getCode())
                .serviceName(q.getServiceName())
                .description(q.getDescription())
                .price(q.getPrice())
                .scheduledAt(q.getScheduledAt())
                .notes(q.getNotes())
                .status(q.getStatus() == null ? null : q.getStatus().apiValue())
                .createdAt(q.getCreatedAt())
                .build();
    }

    public EmbeddedQuotation toEmbedded(Quotation q) {
        if (q == null) return null;
        return EmbeddedQuotation.builder()
                .id(q.getCode())
                .serviceName(q.getServiceName())
                .description(q.getDescription())
                .price(q.getPrice())
                .scheduledAt(q.getScheduledAt())
                .status(q.getStatus() == null ? null : q.getStatus().apiValue())
                .build();
    }

    public AcceptQuotationResponse toAcceptResponse(Quotation q) {
        return AcceptQuotationResponse.builder()
                .id(q.getCode())
                .status(q.getStatus() == null ? null : q.getStatus().apiValue())
                .orderId(q.getOrder() == null ? null : q.getOrder().getCode())
                .acceptedAt(q.getAcceptedAt())
                .build();
    }
}
