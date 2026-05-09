package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuoteResponse {

    private Long id;
    private Long conversationId;
    private Long amount;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
