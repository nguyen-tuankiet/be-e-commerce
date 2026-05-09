package com.example.becommerce.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
}
