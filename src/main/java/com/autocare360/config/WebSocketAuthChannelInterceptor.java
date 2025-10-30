package com.autocare360.config;

import com.autocare360.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT token from STOMP headers
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                try {
                    if (jwtService.isTokenValid(token)) {
                        String email = jwtService.extractSubject(token);
                        log.info("✅ WebSocket authentication successful for user: {}", email);
                        
                        // Create a simple Principal with the user's email
                        Principal principal = () -> email;
                        accessor.setUser(principal);
                    } else {
                        log.warn("❌ WebSocket authentication failed: Invalid token");
                    }
                } catch (Exception e) {
                    log.error("❌ WebSocket authentication error: {}", e.getMessage());
                }
            } else {
                log.warn("⚠️ WebSocket CONNECT without Authorization header");
            }
        }
        
        return message;
    }
}
