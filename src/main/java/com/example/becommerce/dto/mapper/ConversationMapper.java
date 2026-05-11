package com.example.becommerce.dto.mapper;

import com.example.becommerce.dto.response.chat.ChatPartnerSummary;
import com.example.becommerce.dto.response.chat.ConversationCreatedResponse;
import com.example.becommerce.dto.response.chat.ConversationListItemResponse;
import com.example.becommerce.dto.response.chat.LastMessagePreview;
import com.example.becommerce.entity.Conversation;
import com.example.becommerce.entity.Message;
import com.example.becommerce.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMapper {

    public ConversationListItemResponse toListItem(Conversation c, User viewer,
                                                    Message lastMessage,
                                                    long unreadCount) {
        if (c == null) return null;
        User partner = partnerOf(c, viewer);

        return ConversationListItemResponse.builder()
                .id(c.getCode())
                .orderId(c.getOrder() == null ? null : c.getOrder().getCode())
                .partner(partner == null ? null : ChatPartnerSummary.builder()
                        .id(partner.getCode())
                        .fullName(partner.getFullName())
                        .avatar(partner.getAvatar())
                        .isOnline(false) // No presence layer yet
                        .build())
                .lastMessage(lastMessage == null ? null : LastMessagePreview.builder()
                        .content(lastMessage.getContent())
                        .senderId(lastMessage.getSender() == null ? null : lastMessage.getSender().getCode())
                        .sentAt(lastMessage.getSentAt())
                        .build())
                .unreadCount(unreadCount)
                .updatedAt(c.getLastMessageAt() != null ? c.getLastMessageAt() : c.getUpdatedAt())
                .build();
    }

    public ConversationCreatedResponse toCreatedResponse(Conversation c) {
        return ConversationCreatedResponse.builder()
                .id(c.getCode())
                .orderId(c.getOrder() == null ? null : c.getOrder().getCode())
                .participants(List.of(
                        c.getCustomer().getCode(),
                        c.getTechnician().getCode()))
                .createdAt(c.getCreatedAt())
                .build();
    }

    /** Returns the participant that is NOT {@code viewer}. */
    public User partnerOf(Conversation c, User viewer) {
        if (c == null || viewer == null) return null;
        if (c.getCustomer() != null && c.getCustomer().getId().equals(viewer.getId())) {
            return c.getTechnician();
        }
        if (c.getTechnician() != null && c.getTechnician().getId().equals(viewer.getId())) {
            return c.getCustomer();
        }
        return null;
    }
}
