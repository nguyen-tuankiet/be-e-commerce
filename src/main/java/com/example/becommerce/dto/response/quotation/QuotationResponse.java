package com.example.becommerce.dto.response.quotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuotationResponse {

    private String id;
    private String conversationId;
    private String technicianId;

    private String serviceName;
    private String description;
    private Long price;
    private LocalDateTime scheduledAt;
    private String notes;

    private String status;
    private LocalDateTime createdAt;
}
