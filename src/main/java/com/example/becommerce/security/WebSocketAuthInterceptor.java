package com.example.becommerce.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Authenticates STOMP CONNECT frames using the same Bearer JWT scheme
 * as the HTTP layer. The token is read from the {@code Authorization}
 * STOMP header sent by the client on connection.
 *
 * <p>Once authenticated, the principal is attached to the STOMP session
 * so subsequent SEND/SUBSCRIBE frames automatically inherit the identity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String AUTH_HEADER   = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider              jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String header = accessor.getFirstNativeHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            log.debug("STOMP CONNECT without Bearer token — rejected");
            return message;
        }

        String token = header.substring(BEARER_PREFIX.length());
        if (!jwtProvider.isTokenValid(token)) {
            log.debug("STOMP CONNECT with invalid token — rejected");
            return message;
        }

        String email = jwtProvider.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (userDetails instanceof CustomUserDetails cud
                && (!cud.isAccountNonLocked() || !cud.isEnabled())) {
            log.warn("Skipping WS auth for locked/disabled user: {}", email);
            return message;
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        accessor.setUser(authToken);
        log.debug("STOMP CONNECT authenticated for {}", email);
        return message;
    }
}
