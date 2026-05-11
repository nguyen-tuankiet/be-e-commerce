package com.example.becommerce.controller;

import com.example.becommerce.dto.request.chat.CreateConversationRequest;
import com.example.becommerce.dto.request.chat.SendMessageRequest;
import com.example.becommerce.dto.request.quotation.CreateQuotationRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.PagedResponse;
import com.example.becommerce.dto.response.chat.ConversationCreatedResponse;
import com.example.becommerce.dto.response.chat.ConversationListItemResponse;
import com.example.becommerce.dto.response.chat.MessageResponse;
import com.example.becommerce.dto.response.quotation.QuotationResponse;
import com.example.becommerce.service.ConversationService;
import com.example.becommerce.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Chat endpoints. Quote creation is nested under conversations
 * because the FE flow places "Send quote" inside the chat UI.
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Validated
public class ConversationController {

    private final ConversationService conversationService;
    private final QuotationService    quotationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ConversationListItemResponse>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(conversationService.listConversations(page, limit)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationCreatedResponse>> create(
            @Valid @RequestBody CreateConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(conversationService.createConversation(request)));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> listMessages(
            @PathVariable("id") String conversationCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.listMessages(conversationCode, page, limit)));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable("id") String conversationCode,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(conversationService.sendMessage(conversationCode, request)));
    }

    @PostMapping("/{id}/quotes")
    public ResponseEntity<ApiResponse<QuotationResponse>> createQuote(
            @PathVariable("id") String conversationCode,
            @Valid @RequestBody CreateQuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(quotationService.createQuotation(conversationCode, request)));
    }
}
