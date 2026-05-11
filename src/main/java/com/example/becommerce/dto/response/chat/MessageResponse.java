package com.example.becommerce.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {

    private String id;
    private String conversationId;
    private String senderId;

    private String type;
    private String content;

    /** Set only when {@code type == "quotation"}. */
    private EmbeddedQuotation quotation;

    private LocalDateTime sentAt;
    private Boolean isRead;
}
