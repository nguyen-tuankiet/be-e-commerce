package com.example.becommerce.dto.ws;

import com.example.becommerce.dto.response.chat.MessageResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Server -> client. Pushed to {@code /topic/conversations.{conversationCode}}
 * whenever a new message lands in a conversation.
 *
 * <p>Mirrors {@code socket.on("message:new", {...})} from the API spec.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageNewEvent {

    @Builder.Default
    private final String event = "message:new";

    private String conversationId;
    private MessageResponse message;
}
