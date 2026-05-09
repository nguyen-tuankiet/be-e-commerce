package com.example.becommerce.service;

import com.example.becommerce.dto.response.ConversationResponse;
import com.example.becommerce.dto.response.MessageResponse;

import java.util.List;

public interface ConversationService {

    ConversationResponse createConversation(Long orderId);

    List<ConversationResponse> getUserConversations();

    List<MessageResponse> getMessages(Long conversationId);

    MessageResponse sendMessage(Long conversationId, String content);
}
