package com.autocare360.config;

import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import com.autocare360.security.JwtService;
import java.security.Principal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      // Extract JWT token from STOMP headers
      String authHeader = accessor.getFirstNativeHeader("Authorization");

      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);

        try {
          if (jwtService.isTokenValid(token)) {
            // Prefer email/username from token subject; some tokens may contain numeric userId
            String subject = jwtService.extractSubject(token);
            String emailCandidate = subject;
            String resolvedEmail = emailCandidate;

            if (resolvedEmail != null && !resolvedEmail.contains("@")) {
              // Subject appears to be a numeric ID; resolve to email for user-destination routing
              try {
                Long userId = Long.parseLong(resolvedEmail);
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                  resolvedEmail = userOpt.get().getEmail();
                } else {
                  log.warn("❌ WebSocket auth: Could not resolve email for userId {}", userId);
                  resolvedEmail = null;
                }
              } catch (NumberFormatException nfe) {
                log.warn("❌ WebSocket auth: Subject '{}' is not an email or numeric id", subject);
                resolvedEmail = null;
              }
            }

            if (resolvedEmail != null && !resolvedEmail.isBlank()) {
              final String principalEmail = resolvedEmail;
              log.info(
                  "✅ WebSocket authentication successful for user (email): {}", principalEmail);
              // Create a simple Principal with the user's email
              Principal principal = () -> principalEmail;
              accessor.setUser(principal);
            } else {
              log.warn(
                  "❌ WebSocket authentication failed: Could not determine email principal from token");
            }
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
