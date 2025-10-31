package com.autocare360.util;

import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUtil {

    private final UserRepository userRepository;

    /**
     * Extract user ID from the authentication principal
     */
    public Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("Authentication is null or principal is null");
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getPrincipal().toString();
        log.debug("Extracting user ID for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new RuntimeException("User not found with email: " + email);
                });

        log.debug("Found user ID: {} for email: {}", user.getId(), email);
        return user.getId();
    }

    /**
     * Get the full user entity from authentication
     */
    public User getUserFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("Authentication is null or principal is null");
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getPrincipal().toString();
        log.debug("Fetching user for email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new RuntimeException("User not found with email: " + email);
                });
    }
    
    /**
     * Extract user ID from email (used for WebSocket authentication)
     */
    public Long getUserIdFromEmail(String email) {
        log.debug("Extracting user ID for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new RuntimeException("User not found with email: " + email);
                });
        
        log.debug("Found user ID: {} for email: {}", user.getId(), email);
        return user.getId();
    }
}

