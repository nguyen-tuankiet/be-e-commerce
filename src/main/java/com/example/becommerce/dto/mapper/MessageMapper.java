package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.MessageResponse;
import com.example.becommerce.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message message) {
        if (message == null) return null;
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .type(message.getType().name().toLowerCase())
                .content(message.getContent())
                .quoteId(message.getQuoteId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
