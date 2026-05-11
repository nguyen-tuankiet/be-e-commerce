package com.example.becommerce.controller;

import com.example.becommerce.security.CustomUserDetails;
import com.example.becommerce.service.WsEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * STOMP message handlers for chat presence signals.
 *
 * <p>The client emits to:
 *  <ul>
 *    <li>{@code /app/conversations/{id}/typing}</li>
 *    <li>{@code /app/conversations/{id}/stop-typing}</li>
 *  </ul>
 * and the server fans out the corresponding event to the conversation topic.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WsChatController {

    private final WsEventPublisher eventPublisher;

    @MessageMapping("/conversations/{id}/typing")
    public void typing(@DestinationVariable("id") String conversationCode, Authentication auth) {
        eventPublisher.publishTyping(conversationCode, currentUserCode(auth), true);
    }

    @MessageMapping("/conversations/{id}/stop-typing")
    public void stopTyping(@DestinationVariable("id") String conversationCode, Authentication auth) {
        eventPublisher.publishTyping(conversationCode, currentUserCode(auth), false);
    }

    private String currentUserCode(Authentication auth) {
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud && cud.getUser() != null) {
            return cud.getUser().getCode();
        }
        return null;
    }
}
