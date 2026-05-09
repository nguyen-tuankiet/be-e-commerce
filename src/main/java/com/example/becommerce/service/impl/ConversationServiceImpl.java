package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.ConversationMapper;
import com.example.becommerce.dto.mapper.MessageMapper;
import com.example.becommerce.dto.response.ConversationResponse;
import com.example.becommerce.dto.response.MessageResponse;
import com.example.becommerce.entity.Conversation;
import com.example.becommerce.entity.Message;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.User;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.ConversationRepository;
import com.example.becommerce.repository.MessageRepository;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final OrderRepository orderRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public ConversationResponse createConversation(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn hàng với id: " + orderId));

        User currentUser = getCurrentUser();
        verifyParticipant(order, currentUser);

        var existing = conversationRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            Message lastMessage = messageRepository.findLastByConversationId(existing.get().getId())
                    .orElse(null);
            return conversationMapper.toResponse(existing.get(), lastMessage);
        }

        Conversation conversation = Conversation.builder()
                .order(order)
                .build();
        conversation = conversationRepository.save(conversation);
        log.info("Conversation created for order [{}] by user [{}]", order.getCode(), currentUser.getCode());

        return conversationMapper.toResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations() {
        User currentUser = getCurrentUser();
        List<Conversation> conversations = conversationRepository.findAllByParticipantId(currentUser.getId());

        return conversations.stream()
                .map(conv -> {
                    Message lastMessage = messageRepository.findLastByConversationId(conv.getId())
                            .orElse(null);
                    return conversationMapper.toResponse(conv, lastMessage);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy hội thoại với id: " + conversationId));

        User currentUser = getCurrentUser();
        verifyParticipant(conversation.getOrder(), currentUser);

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long conversationId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy hội thoại với id: " + conversationId));

        User currentUser = getCurrentUser();
        verifyParticipant(conversation.getOrder(), currentUser);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(currentUser)
                .content(content)
                .build();
        message = messageRepository.save(message);
        log.info("Message sent by user [{}] in conversation [{}]", currentUser.getCode(), conversationId);

        return messageMapper.toResponse(message);
    }

    private void verifyParticipant(Order order, User user) {
        boolean isCustomer = order.getCustomer().getId().equals(user.getId());
        boolean isTechnician = order.getTechnician() != null
                && order.getTechnician().getId().equals(user.getId());
        if (!isCustomer && !isTechnician) {
            throw AppException.forbidden("Bạn không phải người tham gia của hội thoại này");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw AppException.unauthorized("Chưa xác thực người dùng");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        throw AppException.unauthorized("Không thể xác thực người dùng");
    }
}
