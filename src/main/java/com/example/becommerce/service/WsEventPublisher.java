package com.example.becommerce.service;

import com.example.becommerce.dto.response.chat.MessageResponse;
import com.example.becommerce.dto.ws.MessageNewEvent;
import com.example.becommerce.dto.ws.NotificationNewEvent;
import com.example.becommerce.dto.ws.OrderStatusChangedEvent;
import com.example.becommerce.dto.ws.PriceAdjustmentRequestedEvent;
import com.example.becommerce.dto.ws.TypingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Thin wrapper around {@link SimpMessagingTemplate} for publishing
 * domain events to WebSocket subscribers.
 *
 * <p>All methods are defensive: failures are logged but never propagate.
 * Realtime is a "best effort" enhancement on top of the REST flow — a
 * broker outage must not break the underlying transaction.
 *
 * <p>The {@code SimpMessagingTemplate} bean is created automatically by
 * Spring Boot's WebSocket auto-configuration. We mark the field as
 * {@link Autowired} on the field (not the constructor) so the publisher
 * still wires even if the WebSocket starter is removed in the future.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WsEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    // ===============================================================
    // Destinations
    // ===============================================================

    private static String conversationTopic(String conversationCode) {
        return "/topic/conversations." + conversationCode;
    }

    private static String orderTopic(String orderCode) {
        return "/topic/orders." + orderCode;
    }

    private static String userNotificationQueue(String userCode) {
        return "/topic/notifications." + userCode;
    }

    // ===============================================================
    // Chat events
    // ===============================================================

    public void publishMessageNew(String conversationCode, MessageResponse message) {
        if (conversationCode == null || message == null) return;
        send(conversationTopic(conversationCode),
                MessageNewEvent.builder()
                        .conversationId(conversationCode)
                        .message(message)
                        .build());
    }

    public void publishTyping(String conversationCode, String userCode, boolean typing) {
        if (conversationCode == null) return;
        send(conversationTopic(conversationCode),
                TypingEvent.builder()
                        .event(typing ? "typing" : "stop_typing")
                        .conversationId(conversationCode)
                        .userId(userCode)
                        .build());
    }

    // ===============================================================
    // Order events
    // ===============================================================

    public void publishOrderStatusChanged(String orderCode, String oldStatus, String newStatus) {
        if (orderCode == null) return;
        send(orderTopic(orderCode),
                OrderStatusChangedEvent.builder()
                        .orderId(orderCode)
                        .oldStatus(oldStatus)
                        .newStatus(newStatus)
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    public void publishPriceAdjustmentRequested(String orderCode,
                                                 Long originalPrice,
                                                 Long newPrice,
                                                 String reason) {
        if (orderCode == null) return;
        send(orderTopic(orderCode),
                PriceAdjustmentRequestedEvent.builder()
                        .orderId(orderCode)
                        .originalPrice(originalPrice)
                        .newPrice(newPrice)
                        .reason(reason)
                        .build());
    }

    // ===============================================================
    // Notification events
    // ===============================================================

    public void publishNotificationNew(String userCode,
                                        String notificationCode,
                                        String type,
                                        String title,
                                        String body,
                                        Map<String, Object> data) {
        if (userCode == null || notificationCode == null) return;
        send(userNotificationQueue(userCode),
                NotificationNewEvent.builder()
                        .id(notificationCode)
                        .type(type)
                        .title(title)
                        .body(body)
                        .data(data)
                        .build());
    }

    // ===============================================================
    // Generic safe sender
    // ===============================================================

    private void send(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception ex) {
            // Never let WS failures bubble — they shouldn't break business flow.
            log.warn("WS publish failed for {}: {}", destination, ex.getMessage());
        }
    }
}
