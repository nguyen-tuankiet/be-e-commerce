package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.mapper.QuoteMapper;
import com.example.becommerce.dto.response.QuoteResponse;
import com.example.becommerce.entity.*;
import com.example.becommerce.entity.enums.MessageType;
import com.example.becommerce.entity.enums.QuoteStatus;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.repository.MessageRepository;
import com.example.becommerce.repository.QuoteRepository;
import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.QuoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final MessageRepository messageRepository;
    private final QuoteMapper quoteMapper;

    @Override
    @Transactional
    public QuoteResponse acceptQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy báo giá với id: " + quoteId));

        User customer = getCurrentUser();
        Conversation conversation = quote.getConversation();
        Order order = conversation.getOrder();

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw AppException.forbidden("Bạn không phải chủ của đơn hàng này");
        }
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Báo giá này đã được xử lý trước đó");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote = quoteRepository.save(quote);

        BigDecimal currentTotal = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        order.setTotalPrice(currentTotal.add(BigDecimal.valueOf(quote.getAmount())));

        Message systemMsg = Message.builder()
                .conversation(conversation)
                .sender(customer)
                .type(MessageType.SYSTEM)
                .content("Khách hàng đã chấp nhận báo giá")
                .build();
        messageRepository.save(systemMsg);

        log.info("Quote [{}] accepted by customer [{}], order [{}] total updated to {}",
                quoteId, customer.getCode(), order.getCode(), order.getTotalPrice());

        return quoteMapper.toResponse(quote);
    }

    @Override
    @Transactional
    public QuoteResponse rejectQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy báo giá với id: " + quoteId));

        User customer = getCurrentUser();
        Conversation conversation = quote.getConversation();
        Order order = conversation.getOrder();

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw AppException.forbidden("Bạn không phải chủ của đơn hàng này");
        }
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw AppException.badRequest(ErrorCode.ORDER_INVALID_STATUS,
                    "Báo giá này đã được xử lý trước đó");
        }

        quote.setStatus(QuoteStatus.REJECTED);
        quote = quoteRepository.save(quote);

        Message systemMsg = Message.builder()
                .conversation(conversation)
                .sender(customer)
                .type(MessageType.SYSTEM)
                .content("Khách hàng đã từ chối báo giá")
                .build();
        messageRepository.save(systemMsg);

        log.info("Quote [{}] rejected by customer [{}]", quoteId, customer.getCode());

        return quoteMapper.toResponse(quote);
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
