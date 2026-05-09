package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.ConversationResponse;
import com.example.becommerce.entity.Conversation;
import com.example.becommerce.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(Conversation conversation, Message lastMessage) {
        if (conversation == null) return null;
        return ConversationResponse.builder()
                .id(conversation.getId())
                .orderId(conversation.getOrder().getId())
                .orderCode(conversation.getOrder().getCode())
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public ConversationResponse toResponse(Conversation conversation) {
        return toResponse(conversation, null);
    }
}
