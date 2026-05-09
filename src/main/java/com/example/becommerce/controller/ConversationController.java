package com.example.becommerce.controller;

import com.example.becommerce.constant.ApiConstant;
import com.example.becommerce.dto.request.OrderIdRequest;
import com.example.becommerce.dto.request.SendMessageRequest;
import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.ConversationResponse;
import com.example.becommerce.dto.response.MessageResponse;
import com.example.becommerce.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstant.CONVERSATION_BASE)
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @Valid @RequestBody OrderIdRequest request) {
        ConversationResponse data = conversationService.createConversation(request.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getUserConversations() {
        List<ConversationResponse> data = conversationService.getUserConversations();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(@PathVariable Long id) {
        List<MessageResponse> data = conversationService.getMessages(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse data = conversationService.sendMessage(id, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }
}
