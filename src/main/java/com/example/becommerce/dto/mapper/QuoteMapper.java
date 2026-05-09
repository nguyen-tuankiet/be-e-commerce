package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.QuoteResponse;
import com.example.becommerce.entity.Quote;
import org.springframework.stereotype.Component;

@Component
public class QuoteMapper {

    public QuoteResponse toResponse(Quote quote) {
        if (quote == null) return null;
        return QuoteResponse.builder()
                .id(quote.getId())
                .conversationId(quote.getConversation().getId())
                .amount(quote.getAmount())
                .description(quote.getDescription())
                .status(quote.getStatus().name().toLowerCase())
                .createdAt(quote.getCreatedAt())
                .build();
    }
}
