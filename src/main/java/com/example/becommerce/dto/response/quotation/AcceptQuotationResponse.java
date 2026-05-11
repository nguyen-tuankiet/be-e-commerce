package com.example.becommerce.dto.response.quotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AcceptQuotationResponse {

    private String id;
    private String status;
    private String orderId;
    private LocalDateTime acceptedAt;
}
