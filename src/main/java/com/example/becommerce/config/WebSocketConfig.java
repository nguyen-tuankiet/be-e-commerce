package com.example.becommerce.config;

import com.example.becommerce.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket configuration.
 *
 * <p>Topology:
 *  <ul>
 *    <li><b>Client → Server</b> messages target {@code /app/...} prefixes,
 *        handled by {@code @MessageMapping} controllers.</li>
 *    <li><b>Server → Client</b> broadcasts go to {@code /topic/...} (group fan-out)
 *        or {@code /queue/...} (user-targeted).</li>
 *  </ul>
 *
 * <p>The endpoint {@code /ws} is exposed with SockJS fallback so browsers that
 * cannot use raw WebSockets still work.
 *
 * <p>The spec's Socket.IO event names map to destinations + payload {@code event} field:
 *  <pre>
 *  socket.on("message:new", payload)  ≡  STOMP frame on /topic/conversations.{id}
 *                                          with body { "event": "message:new", ... }
 *  socket.on("order:status_changed", payload)  ≡  /topic/orders.{id}
 *  socket.on("notification:new", payload)      ≡  /queue/notifications.{userCode}
 *  </pre>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Outbound destinations the broker handles.
        registry.enableSimpleBroker("/topic", "/queue");
        // Inbound @MessageMapping prefix.
        registry.setApplicationDestinationPrefixes("/app");
        // Convention for sending to a specific user, e.g. convertAndSendToUser(userCode, "/queue/notifications", ...)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Authenticate every inbound STOMP frame using the JWT carried on CONNECT.
        registration.interceptors(authInterceptor);
    }
}
