package com.autocare360.service;

import com.autocare360.dto.*;
import com.autocare360.entity.Notification;
import com.autocare360.entity.NotificationPreference;
import com.autocare360.repo.NotificationPreferenceRepository;
import com.autocare360.repo.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationPreferenceRepository preferenceRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  /** Send a notification to a specific user */
  @Transactional
  public void sendNotificationToUser(
      Long userId, String type, String title, String message, Map<String, Object> data) {
    try {
      // Check user preferences
      NotificationPreference preference =
          preferenceRepository.findByUserId(userId).orElse(createDefaultPreferenceSafely(userId));

      if (!shouldSendNotification(preference, type)) {
        log.info("Notification blocked by user preference: userId={}, type={}", userId, type);
        return;
      }

      // Convert data map to JSON string
      String dataJson = data != null ? objectMapper.writeValueAsString(data) : null;

      // Save notification to database
      Notification notification =
          Notification.builder()
              .userId(userId)
              .type(type)
              .title(title)
              .message(message)
              .data(dataJson)
              .isRead(false)
              .build();

      notification = notificationRepository.save(notification);

      // Send real-time notification via WebSocket
      NotificationMessage wsMessage =
          NotificationMessage.builder()
              .id(notification.getId())
              .type(type)
              .title(title)
              .message(message)
              .userId(userId)
              .timestamp(notification.getCreatedAt())
              .isRead(false)
              .data(data)
              .build();

      messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", wsMessage);

      log.info("Notification sent to user {}: {}", userId, title);

    } catch (JsonProcessingException e) {
      log.error("Error converting notification data to JSON", e);
    }
  }

  /** Notify service progress update */
  public void notifyServiceProgress(ServiceProgressUpdate update) {
    Map<String, Object> data = new HashMap<>();
    data.put("serviceId", update.getServiceId());
    data.put("vehicleNumber", update.getVehicleNumber());
    data.put("status", update.getStatus());
    data.put("progressPercentage", update.getProgressPercentage());
    data.put("currentStage", update.getCurrentStage());

    String title = "Service Progress Update";
    String message =
        String.format(
            "Your %s for vehicle %s is now %s (%d%% complete)",
            update.getServiceName(),
            update.getVehicleNumber(),
            update.getStatus(),
            update.getProgressPercentage());

    sendNotificationToUser(update.getCustomerId(), "SERVICE_UPDATE", title, message, data);

    // Also broadcast to topic for real-time dashboard updates
    messagingTemplate.convertAndSend("/topic/service-updates", update);
  }

  /** Notify appointment confirmation or update */
  public void notifyAppointment(AppointmentNotification appointmentNotification) {
    Map<String, Object> data = new HashMap<>();
    data.put("appointmentId", appointmentNotification.getAppointmentId());
    data.put("appointmentDate", appointmentNotification.getAppointmentDate());
    data.put("appointmentTime", appointmentNotification.getAppointmentTime());
    data.put("serviceType", appointmentNotification.getServiceType());
    data.put("vehicleNumber", appointmentNotification.getVehicleNumber());
    data.put("status", appointmentNotification.getStatus());

    String title = "Appointment " + appointmentNotification.getStatus();

    sendNotificationToUser(
        appointmentNotification.getCustomerId(),
        "APPOINTMENT_UPDATE",
        title,
        appointmentNotification.getMessage(),
        data);
  }

  /** Notify new project/modification request */
  public void notifyProjectRequest(Long userId, Long projectId, String projectName, String status) {
    Map<String, Object> data = new HashMap<>();
    data.put("projectId", projectId);
    data.put("projectName", projectName);
    data.put("status", status);

    String title = "Project Request " + status;
    String message =
        String.format(
            "Your modification request '%s' has been %s", projectName, status.toLowerCase());

    sendNotificationToUser(userId, "PROJECT_UPDATE", title, message, data);
  }

  /** Broadcast system-wide announcement */
  public void broadcastAnnouncement(String title, String message) {
    NotificationMessage announcement =
        NotificationMessage.builder()
            .type("ANNOUNCEMENT")
            .title(title)
            .message(message)
            .timestamp(Instant.now())
            .build();

    messagingTemplate.convertAndSend("/topic/announcements", announcement);
    log.info("Broadcast announcement: {}", title);
  }

  /** Get all notifications for a user */
  public List<NotificationResponse> getUserNotifications(Long userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /** Get unread notifications for a user */
  public List<NotificationResponse> getUnreadNotifications(Long userId) {
    return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /** Get unread notification count */
  public Long getUnreadCount(Long userId) {
    return notificationRepository.countByUserIdAndIsRead(userId, false);
  }

  /** Mark notification as read */
  @Transactional
  public void markAsRead(Long notificationId, Long userId) {
    notificationRepository
        .findById(notificationId)
        .ifPresent(
            notification -> {
              if (notification.getUserId().equals(userId)) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
                log.info("Notification {} marked as read by user {}", notificationId, userId);
              }
            });
  }

  /** Mark all notifications as read for a user */
  @Transactional
  public void markAllAsRead(Long userId) {
    List<Notification> unreadNotifications =
        notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);

    unreadNotifications.forEach(notification -> notification.setIsRead(true));
    notificationRepository.saveAll(unreadNotifications);

    log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
  }

  /** Delete a notification */
  @Transactional
  public void deleteNotification(Long notificationId, Long userId) {
    notificationRepository
        .findById(notificationId)
        .ifPresent(
            notification -> {
              if (notification.getUserId().equals(userId)) {
                notificationRepository.delete(notification);
                log.info("Notification {} deleted by user {}", notificationId, userId);
              }
            });
  }

  /** Get user notification preferences */
  public NotificationPreferenceResponse getUserPreferences(Long userId) {
    log.info("Getting notification preferences for user ID: {}", userId);
    NotificationPreference preference =
        preferenceRepository
            .findByUserId(userId)
            .orElseGet(() -> createDefaultPreferenceSafely(userId));

    log.info("Returning preferences for user {}: {}", userId, preference.getId());
    return mapPreferenceToResponse(preference);
  }

  /** Update user notification preferences */
  @Transactional
  public NotificationPreferenceResponse updateUserPreferences(
      Long userId, NotificationPreferenceRequest request) {
    log.info("Updating notification preferences for user ID: {}", userId);
    NotificationPreference preference =
        preferenceRepository
            .findByUserId(userId)
            .orElseGet(() -> createDefaultPreferenceSafely(userId));

    if (request.getEmailNotifications() != null) {
      preference.setEmailNotifications(request.getEmailNotifications());
    }
    if (request.getPushNotifications() != null) {
      preference.setPushNotifications(request.getPushNotifications());
    }
    if (request.getServiceUpdates() != null) {
      preference.setServiceUpdates(request.getServiceUpdates());
    }
    if (request.getAppointmentReminders() != null) {
      preference.setAppointmentReminders(request.getAppointmentReminders());
    }

    preference = preferenceRepository.save(preference);
    log.info("Updated notification preferences for user {}", userId);

    return mapPreferenceToResponse(preference);
  }

  // Helper methods

  private boolean shouldSendNotification(NotificationPreference preference, String type) {
    if (!preference.getPushNotifications()) {
      return false;
    }

    return switch (type) {
      case "SERVICE_UPDATE" -> preference.getServiceUpdates();
      case "APPOINTMENT_UPDATE" -> preference.getAppointmentReminders();
      default -> true;
    };
  }

  private NotificationPreference createDefaultPreferenceSafely(Long userId) {
    log.info("Creating default notification preferences for user {}", userId);
    try {
      // Check one more time if preferences exist (race condition protection)
      Optional<NotificationPreference> existing = preferenceRepository.findByUserId(userId);
      if (existing.isPresent()) {
        log.info("Preferences already exist for user {}, returning existing", userId);
        return existing.get();
      }

      NotificationPreference preference =
          NotificationPreference.builder()
              .userId(userId)
              .emailNotifications(true)
              .pushNotifications(true)
              .serviceUpdates(true)
              .appointmentReminders(true)
              .build();

      preference = preferenceRepository.save(preference);
      log.info("Created default notification preferences for user {}", userId);
      return preference;
      
    } catch (Exception e) {
      // Handle constraint violation or any other database error
      log.warn("Error creating default preferences for user {}, attempting to fetch existing: {}", userId, e.getMessage());
      
      // Try to fetch existing preferences one more time
      return preferenceRepository.findByUserId(userId)
          .orElseThrow(() -> new RuntimeException("Failed to create or retrieve notification preferences for user " + userId));
    }
  }

  private NotificationResponse mapToResponse(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .type(notification.getType())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .data(notification.getData())
        .build();
  }

  private NotificationPreferenceResponse mapPreferenceToResponse(
      NotificationPreference preference) {
    return NotificationPreferenceResponse.builder()
        .id(preference.getId())
        .userId(preference.getUserId())
        .emailNotifications(preference.getEmailNotifications())
        .pushNotifications(preference.getPushNotifications())
        .serviceUpdates(preference.getServiceUpdates())
        .appointmentReminders(preference.getAppointmentReminders())
        .build();
  }
}
