package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.MessageMapper;
import com.example.becommerce.dto.mapper.QuotationMapper;
import com.example.becommerce.dto.request.quotation.CreateQuotationRequest;
import com.example.becommerce.dto.response.quotation.AcceptQuotationResponse;
import com.example.becommerce.dto.response.quotation.QuotationResponse;
import com.example.becommerce.entity.Conversation;
import com.example.becommerce.entity.Message;
import com.example.becommerce.entity.Order;
import com.example.becommerce.entity.Quotation;
import com.example.becommerce.entity.User;
import com.example.becommerce.entity.enums.MessageType;
import com.example.becommerce.entity.enums.NotificationType;
import com.example.becommerce.entity.enums.OrderStatus;
import com.example.becommerce.entity.enums.QuotationStatus;
import com.example.becommerce.entity.enums.Role;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.ConversationRepository;
import com.example.becommerce.repository.MessageRepository;
import com.example.becommerce.repository.OrderRepository;
import com.example.becommerce.repository.QuotationRepository;
import com.example.becommerce.repository.UserRepository;
import com.example.becommerce.service.NotificationService;
import com.example.becommerce.service.QuotationService;
import com.example.becommerce.service.WsEventPublisher;
import com.example.becommerce.utils.MessageCodeGenerator;
import com.example.becommerce.utils.OrderCodeGenerator;
import com.example.becommerce.utils.QuotationCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Business rules for quotations:
 *  - Only the technician of a conversation can post a quote.
 *  - Posting a quote also writes a "quotation" message into the conversation.
 *  - Only the customer of the conversation can accept it.
 *  - On accept, a new Order is spawned in SCHEDULED status, linking back to the quote.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository      quotationRepository;
    private final ConversationRepository   conversationRepository;
    private final MessageRepository        messageRepository;
    private final OrderRepository          orderRepository;
    private final UserRepository           userRepository;
    private final QuotationMapper          quotationMapper;
    private final MessageMapper            messageMapper;
    private final QuotationCodeGenerator   quotationCodeGenerator;
    private final MessageCodeGenerator     messageCodeGenerator;
    private final OrderCodeGenerator       orderCodeGenerator;
    private final WsEventPublisher         eventPublisher;
    private final NotificationService      notificationService;

    // ===============================================================
    // CREATE QUOTE
    // ===============================================================

    @Override
    @Transactional
    public QuotationResponse createQuotation(String conversationCode, CreateQuotationRequest request) {
        Conversation conversation = findConversation(conversationCode);
        User sender = getCurrentUser();

        if (sender.getRole() != Role.TECHNICIAN) {
            throw AppException.forbidden("Chỉ thợ mới có thể tạo báo giá");
        }
        if (conversation.getTechnician() == null
                || !conversation.getTechnician().getId().equals(sender.getId())) {
            throw AppException.forbidden("Bạn không phải thợ của cuộc trò chuyện này");
        }

        Quotation quotation = Quotation.builder()
                .code(quotationCodeGenerator.generate())
                .conversation(conversation)
                .technician(sender)
                .serviceName(request.getServiceName())
                .description(request.getDescription())
                .price(request.getPrice())
                .scheduledAt(request.getScheduledAt())
                .notes(request.getNotes())
                .status(QuotationStatus.PENDING)
                .build();
        Quotation savedQuote = quotationRepository.save(quotation);

        // Embed the quote in the conversation thread.
        Message message = Message.builder()
                .code(messageCodeGenerator.generate())
                .conversation(conversation)
                .sender(sender)
                .type(MessageType.QUOTATION)
                .content(null)
                .quotation(savedQuote)
                .build();
        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessageAt(savedMessage.getSentAt());
        conversationRepository.save(conversation);

        // Push the embedded quotation message to chat subscribers.
        eventPublisher.publishMessageNew(
                conversation.getCode(),
                messageMapper.toResponse(savedMessage, sender, savedMessage.getSentAt()));

        log.info("Quotation {} posted by technician {} in conversation {}",
                savedQuote.getCode(), sender.getCode(), conversation.getCode());

        return quotationMapper.toResponse(savedQuote);
    }

    // ===============================================================
    // ACCEPT QUOTE → SPAWN ORDER
    // ===============================================================

    @Override
    @Transactional
    public AcceptQuotationResponse acceptQuotation(String quotationCode) {
        Quotation quotation = quotationRepository.findByCode(quotationCode)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy báo giá " + quotationCode));

        Conversation conversation = quotation.getConversation();
        User customer = getCurrentUser();

        if (customer.getRole() != Role.CUSTOMER
                || conversation.getCustomer() == null
                || !conversation.getCustomer().getId().equals(customer.getId())) {
            throw AppException.forbidden("Chỉ khách hàng của cuộc trò chuyện mới có thể chấp nhận báo giá");
        }
        if (quotation.getStatus() != QuotationStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.QUOTATION_NOT_PENDING,
                    "Báo giá không còn ở trạng thái chờ duyệt");
        }

        Order order = Order.builder()
                .code(orderCodeGenerator.generate())
                .customer(customer)
                .technician(quotation.getTechnician())
                .serviceName(quotation.getServiceName())
                .description(quotation.getDescription())
                .estimatedPrice(quotation.getPrice())
                .scheduledAt(quotation.getScheduledAt())
                .expectedTime(quotation.getScheduledAt())
                .status(OrderStatus.SCHEDULED)
                .build();
        if (conversation.getOrder() == null) {
            // First time the conversation gets an order — link it.
            conversation.setOrder(order);
        }
        Order savedOrder = orderRepository.save(order);

        quotation.setStatus(QuotationStatus.ACCEPTED);
        quotation.setAcceptedAt(LocalDateTime.now());
        quotation.setOrder(savedOrder);
        Quotation savedQuote = quotationRepository.save(quotation);
        conversationRepository.save(conversation);

        // New order is born SCHEDULED — emit so both parties can react.
        eventPublisher.publishOrderStatusChanged(
                savedOrder.getCode(), null, OrderStatus.SCHEDULED.apiValue());

        // Notify the technician their quote was accepted.
        try {
            notificationService.createNotification(
                    quotation.getTechnician(),
                    NotificationType.ORDER_ACCEPTED,
                    "Báo giá được chấp nhận",
                    "Khách " + customer.getFullName() + " đã chấp nhận báo giá "
                            + savedQuote.getCode() + " → đơn " + savedOrder.getCode(),
                    java.util.Map.of(
                            "quotationId", savedQuote.getCode(),
                            "orderId", savedOrder.getCode()));
        } catch (Exception ex) {
            log.warn("Failed to notify technician on quote acceptance: {}", ex.getMessage());
        }

        log.info("Quotation {} accepted by customer {} → order {}",
                savedQuote.getCode(), customer.getCode(), savedOrder.getCode());

        return quotationMapper.toAcceptResponse(savedQuote);
    }

    // ----------------------------------------------------------------

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
