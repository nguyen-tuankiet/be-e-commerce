package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.chat.MessageResponse;
import com.example.becommerce.entity.Conversation;
import com.example.becommerce.entity.Message;
import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageMapper {

    private final QuotationMapper quotationMapper;

    public MessageMapper(QuotationMapper quotationMapper) {
        this.quotationMapper = quotationMapper;
    }

    /**
     * @param viewer       the user reading the message — used to derive {@code isRead}
     * @param watermark    the viewer's {@code lastReadAt} on the conversation
     */
    public MessageResponse toResponse(Message m, User viewer, LocalDateTime watermark) {
        if (m == null) return null;

        Conversation conv = m.getConversation();
        boolean isRead;
        if (viewer != null && m.getSender() != null && m.getSender().getId().equals(viewer.getId())) {
            // Own messages are always considered read by the sender.
            isRead = true;
        } else {
            isRead = watermark != null && m.getSentAt() != null && !m.getSentAt().isAfter(watermark);
        }

        return MessageResponse.builder()
                .id(m.getCode())
                .conversationId(conv == null ? null : conv.getCode())
                .senderId(m.getSender() == null ? null : m.getSender().getCode())
                .type(m.getType() == null ? null : m.getType().apiValue())
                .content(m.getContent())
                .quotation(quotationMapper.toEmbedded(m.getQuotation()))
                .sentAt(m.getSentAt())
                .isRead(isRead)
                .build();
    }
}
