package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.ConversationMapper;
import com.example.becommerce.dto.mapper.MessageMapper;
import com.example.becommerce.dto.request.chat.CreateConversationRequest;
import com.example.becommerce.dto.request.chat.SendMessageRequest;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.chat.ConversationCreatedResponse;
import com.example.becommerce.dto.response.chat.ConversationListItemResponse;
import com.example.becommerce.dto.response.chat.MessageResponse;
import com.example.becommerce.entity.Conversation;
import com.example.becommerce.entity.Message;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.MessageType;
import com.example.becommerce.entity.enums.NotificationType;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.ConversationRepository;
import com.example.becommerce.repository.MessageRepository;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.service.ConversationService;
import com.example.becommerce.service.NotificationService;
import com.example.becommerce.service.WsEventPublisher;
import com.example.becommerce.utils.ConversationCodeGenerator;
import com.example.becommerce.utils.MessageCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business rules for chat:
 *  - A conversation links exactly one customer and one technician.
 *  - Customers and technicians can both list & read; the participant model
 *    keys off whichever side the caller belongs to.
 *  - {@link Conversation#getCustomerLastReadAt()} /
 *    {@link Conversation#getTechnicianLastReadAt()} act as the read watermark.
 *  - Calling GET /messages bumps the watermark for the caller; this is how
 *    unread counters reset.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository      conversationRepository;
    private final MessageRepository           messageRepository;
    private final OrderRepository             orderRepository;
    private final UserRepository              userRepository;
    private final ConversationMapper          conversationMapper;
    private final MessageMapper               messageMapper;
    private final ConversationCodeGenerator   conversationCodeGenerator;
    private final MessageCodeGenerator        messageCodeGenerator;
    private final WsEventPublisher            eventPublisher;
    private final NotificationService         notificationService;

    // ===============================================================
    // LIST CONVERSATIONS
    // ===============================================================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ConversationListItemResponse> listConversations(int page, int limit) {
        User viewer = getCurrentUser();

        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                limit);

        Page<Conversation> convPage = conversationRepository.findAllForUser(viewer.getId(), pageable);

        List<ConversationListItemResponse> items = convPage.getContent().stream()
                .map(c -> {
                    Message last = messageRepository
                            .findTopByConversation_IdOrderBySentAtDesc(c.getId())
                            .orElse(null);
                    LocalDateTime watermark = readWatermark(c, viewer);
                    long unread = countUnreadForParticipant(c.getId(), viewer.getId(), watermark);
                    return conversationMapper.toListItem(c, viewer, last, unread);
                })
                .filter(item -> item != null && item.getPartner() != null)
                .toList();

        return PagedResponse.of(items, page, limit, convPage.getTotalElements());
    }

    // ===============================================================
    // CREATE CONVERSATION
    // ===============================================================

    @Override
    @Transactional
    public ConversationCreatedResponse createConversation(CreateConversationRequest request) {
        User customer = getCurrentUser();
        if (customer.getRole() != Role.CUSTOMER) {
            throw AppException.forbidden("Chỉ khách hàng mới có thể mở cuộc trò chuyện");
        }

        User technician = userRepository.findByCodeAndDeletedFalse(request.getTechnicianId())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy thợ " + request.getTechnicianId()));
        if (technician.getRole() != Role.TECHNICIAN) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR,
                    "Tài khoản đối tác không phải là thợ");
        }

        Optional<Conversation> existing = conversationRepository
                .findByCustomer_IdAndTechnician_Id(customer.getId(), technician.getId());
        boolean isNewConversation = existing.isEmpty();

        Conversation conversation = existing.orElseGet(() -> Conversation.builder()
                .code(conversationCodeGenerator.generate())
                .customer(customer)
                .technician(technician)
                .build());

        if (request.getOrderId() != null && !request.getOrderId().isBlank()) {
            Order order = orderRepository.findByCodeAndDeletedFalse(request.getOrderId())
                    .orElseThrow(() -> AppException.notFound("Không tìm thấy đơn " + request.getOrderId()));
            conversation.setOrder(order);
        }

        Conversation saved = conversationRepository.save(conversation);
        log.info("Conversation {} between customer={} technician={} (order={})",
                saved.getCode(), customer.getCode(), technician.getCode(),
                saved.getOrder() == null ? "-" : saved.getOrder().getCode());

        if (isNewConversation) {
            notifyChatRecipient(
                    technician,
                    customer.getFullName(),
                    "Khách hàng mới muốn trò chuyện",
                    customer.getFullName() + " đã mở cuộc trò chuyện với bạn.",
                    saved);
        }

        return conversationMapper.toCreatedResponse(saved, customer);
    }

    // ===============================================================
    // LIST MESSAGES
    // ===============================================================

    @Override
    @Transactional
    public PagedResponse<MessageResponse> listMessages(String conversationCode, int page, int limit) {
        Conversation conversation = findConversation(conversationCode);
        User viewer = getCurrentUser();
        ensureParticipant(conversation, viewer);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Message> msgPage = messageRepository.findByConversation_IdOrderBySentAtDesc(
                conversation.getId(), pageable);

        // Snapshot watermark BEFORE updating it so we still mark current page
        // as read appropriately on the FE.
        LocalDateTime watermark = readWatermark(conversation, viewer);

        List<MessageResponse> items = msgPage.getContent().stream()
                .map(m -> messageMapper.toResponse(m, viewer, watermark))
                .toList();

        // Bump caller's watermark to NOW
        LocalDateTime now = LocalDateTime.now();
        if (viewer.getId().equals(conversation.getCustomer().getId())) {
            conversation.setCustomerLastReadAt(now);
        } else {
            conversation.setTechnicianLastReadAt(now);
        }
        conversationRepository.save(conversation);

        return PagedResponse.of(items, page, limit, msgPage.getTotalElements());
    }

    // ===============================================================
    // SEND MESSAGE
    // ===============================================================

    @Override
    @Transactional
    public MessageResponse sendMessage(String conversationCode, SendMessageRequest request) {
        Conversation conversation = findConversation(conversationCode);
        User sender = getCurrentUser();
        ensureParticipant(conversation, sender);

        MessageType type;
        try {
            type = MessageType.from(request.getType());
        } catch (IllegalArgumentException ex) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Loại tin nhắn không hợp lệ");
        }
        if (type == MessageType.QUOTATION) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR,
                    "Quotation phải được tạo qua /quotes endpoint");
        }

        Message message = Message.builder()
                .code(messageCodeGenerator.generate())
                .conversation(conversation)
                .sender(sender)
                .type(type)
                .content(request.getContent())
                .build();
        Message saved = messageRepository.save(message);

        // Touch conversation so listing sorts correctly.
        conversation.setLastMessageAt(saved.getSentAt());
        conversationRepository.save(conversation);

        MessageResponse response = messageMapper.toResponse(saved, sender, readWatermark(conversation, sender));
        eventPublisher.publishMessageNew(conversation.getCode(), response);

        User recipient = conversationMapper.partnerOf(conversation, sender);
        if (recipient != null) {
            String preview = request.getContent() == null ? "" : request.getContent().trim();
            if (preview.length() > 120) {
                preview = preview.substring(0, 117) + "...";
            }
            notifyChatRecipient(
                    recipient,
                    sender.getFullName(),
                    "Tin nhắn mới từ " + sender.getFullName(),
                    preview.isBlank() ? "Bạn có tin nhắn mới" : preview,
                    conversation);
        }

        return response;
    }

    // ===============================================================
    // Helpers
    // ===============================================================

    private void notifyChatRecipient(User recipient, String senderName, String title, String body,
                                     Conversation conversation) {
        try {
            notificationService.createNotification(
                    recipient,
                    NotificationType.CHAT_MESSAGE,
                    title,
                    body,
                    Map.of(
                            "conversationId", conversation.getCode(),
                            "senderName", senderName,
                            "orderId", conversation.getOrder() == null
                                    ? ""
                                    : conversation.getOrder().getCode()));
        } catch (Exception ex) {
            log.warn("Failed to push chat notification to {}: {}", recipient.getCode(), ex.getMessage());
        }
    }

    private long countUnreadForParticipant(Long conversationId, Long userId, LocalDateTime watermark) {
        if (watermark == null) {
            return messageRepository.countUnreadAll(conversationId, userId);
        }
        return messageRepository.countUnreadSince(conversationId, userId, watermark);
    }

    private LocalDateTime readWatermark(Conversation conversation, User user) {
        if (conversation.getCustomer() != null && conversation.getCustomer().getId().equals(user.getId())) {
            return conversation.getCustomerLastReadAt();
        }
        if (conversation.getTechnician() != null && conversation.getTechnician().getId().equals(user.getId())) {
            return conversation.getTechnicianLastReadAt();
        }
        return null;
    }

    private void ensureParticipant(Conversation conversation, User user) {
        if (user.getRole() == Role.ADMIN) return;
        boolean ok = (conversation.getCustomer() != null
                        && conversation.getCustomer().getId().equals(user.getId()))
                || (conversation.getTechnician() != null
                        && conversation.getTechnician().getId().equals(user.getId()));
        if (!ok) {
            throw AppException.forbidden("Bạn không phải thành viên của cuộc trò chuyện này");
        }
    }

    private Conversation findConversation(String code) {
        return conversationRepository.findByCode(code)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy cuộc trò chuyện " + code));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw AppException.unauthorized("Người dùng chưa đăng nhập");
        }
        String email = authentication.getName();
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng hiện tại"));
    }
}
