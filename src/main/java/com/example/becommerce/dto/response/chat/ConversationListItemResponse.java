package com.example.becommerce.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationListItemResponse {

    private String id;
    private String orderId;

    private ChatPartnerSummary partner;
    private LastMessagePreview lastMessage;

    private Long unreadCount;
    private LocalDateTime updatedAt;
}
