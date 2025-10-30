package com.autocare360.controller;

import com.autocare360.dto.AppointmentNotification;
import com.autocare360.dto.ServiceProgressUpdate;
import com.autocare360.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for handling real-time messaging
 */
@Controller
@RequiredArgsConstructor
public class WebSocketNotificationController {

    private final NotificationService notificationService;

    /**
     * Handle service progress updates from employees
     * Employees can send updates that will be broadcast to relevant customers
     */
    @MessageMapping("/service/progress")
    @SendTo("/topic/service-updates")
    public ServiceProgressUpdate handleServiceProgress(@Payload ServiceProgressUpdate update) {
        // Process and notify the customer
        notificationService.notifyServiceProgress(update);
        return update;
    }

    /**
     * Handle appointment notifications
     */
    @MessageMapping("/appointment/notify")
    public void handleAppointmentNotification(@Payload AppointmentNotification notification) {
        notificationService.notifyAppointment(notification);
    }

    /**
     * Handle user connection event
     */
    @MessageMapping("/user/connect")
    public void handleUserConnect(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        // You can track active users here if needed
        System.out.println("User connected: " + sessionId);
    }

    /**
     * Broadcast system announcement
     */
    @MessageMapping("/admin/announce")
    @SendTo("/topic/announcements")
    public void broadcastAnnouncement(@Payload String announcement) {
        notificationService.broadcastAnnouncement("System Announcement", announcement);
    }
}

