package com.autocare360.controller;

import com.autocare360.dto.*;
import com.autocare360.service.NotificationService;
import com.autocare360.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    /**
     * Get all notifications for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        Long userId = authUtil.getUserIdFromAuth(authentication);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        Long userId = authUtil.getUserIdFromAuth(authentication);
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = authUtil.getUserIdFromAuth(authentication);
        Long count = notificationService.getUnreadCount(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Mark a notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = authUtil.getUserIdFromAuth(authentication);
        notificationService.markAsRead(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read");

        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        Long userId = authUtil.getUserIdFromAuth(authentication);
        notificationService.markAllAsRead(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = authUtil.getUserIdFromAuth(authentication);
        notificationService.deleteNotification(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get user notification preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(Authentication authentication) {
        log.info("GET /api/notifications/preferences - Authentication: {}", authentication != null ? authentication.getName() : "null");
        Long userId = authUtil.getUserIdFromAuth(authentication);
        log.info("Fetching preferences for user ID: {}", userId);
        NotificationPreferenceResponse preferences = notificationService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update user notification preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @RequestBody NotificationPreferenceRequest request,
            Authentication authentication) {
        log.info("PUT /api/notifications/preferences - Authentication: {}", authentication != null ? authentication.getName() : "null");
        log.info("Request body: {}", request);
        Long userId = authUtil.getUserIdFromAuth(authentication);
        log.info("Updating preferences for user ID: {}", userId);
        NotificationPreferenceResponse preferences = notificationService.updateUserPreferences(userId, request);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Test endpoint - Send a test notification (for development/testing)
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestNotification(Authentication authentication) {
        Long userId = authUtil.getUserIdFromAuth(authentication);

        Map<String, Object> data = new HashMap<>();
        data.put("testData", "This is a test notification");

        notificationService.sendNotificationToUser(
                userId,
                "TEST",
                "Test Notification",
                "This is a test notification to verify the real-time notification system is working correctly.",
                data
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "Test notification sent");

        return ResponseEntity.ok(response);
    }
}

