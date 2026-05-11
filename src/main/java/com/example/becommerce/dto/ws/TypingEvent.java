package com.example.becommerce.dto.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Client -> server -> other participants (bidirectional).
 *
 * <p>Inbound: client sends to {@code /app/conversations/{id}/typing} or
 * {@code /app/conversations/{id}/stop-typing}.
 *
 * <p>Outbound: server fans out to {@code /topic/conversations.{conversationCode}}
 * so the other participant's UI can show the indicator.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypingEvent {

    /** "typing" | "stop_typing" */
    private String event;

    private String conversationId;
    private String userId;
}
