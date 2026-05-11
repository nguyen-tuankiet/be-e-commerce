package com.example.becommerce.service;

import com.example.becommerce.dto.request.chat.CreateConversationRequest;
import com.example.becommerce.dto.request.chat.SendMessageRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.chat.ConversationCreatedResponse;
import com.example.becommerce.dto.response.chat.ConversationListItemResponse;
import com.example.becommerce.dto.response.chat.MessageResponse;

public interface ConversationService {

    PagedResponse<ConversationListItemResponse> listConversations(int page, int limit);

    ConversationCreatedResponse createConversation(CreateConversationRequest request);

    PagedResponse<MessageResponse> listMessages(String conversationCode, int page, int limit);

    MessageResponse sendMessage(String conversationCode, SendMessageRequest request);
}
