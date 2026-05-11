package com.example.becommerce.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationCreatedResponse {

    private String id;
    private String orderId;
    private List<String> participants;
    private LocalDateTime createdAt;
}
