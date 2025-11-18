package com.autocare360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.autocare360.dto.*;
import com.autocare360.entity.Notification;
import com.autocare360.entity.NotificationPreference;
import com.autocare360.repo.NotificationPreferenceRepository;
import com.autocare360.repo.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private NotificationPreferenceRepository preferenceRepository;

  @Mock private SimpMessagingTemplate messagingTemplate;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private NotificationService notificationService;

  private Notification testNotification;
  private NotificationPreference testPreference;

  @BeforeEach
  void setUp() {
    testNotification =
        Notification.builder()
            .id(1L)
            .userId(1L)
            .type("SERVICE_UPDATE")
            .title("Test Notification")
            .message("Test message")
            .data("{\"key\":\"value\"}")
            .isRead(false)
            .createdAt(Instant.now())
            .build();

    testPreference =
        NotificationPreference.builder()
            .id(1L)
            .userId(1L)
            .emailNotifications(true)
            .pushNotifications(true)
            .serviceUpdates(true)
            .appointmentReminders(true)
            .build();
  }

  @Test
  @DisplayName("Should send notification to user successfully")
  void testSendNotificationToUser_Success() throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("key", "value");

    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
    when(objectMapper.writeValueAsString(data)).thenReturn("{\"key\":\"value\"}");
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
    doNothing()
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(NotificationMessage.class));

    notificationService.sendNotificationToUser(1L, "SERVICE_UPDATE", "Title", "Message", data);

    verify(preferenceRepository, times(1)).findByUserId(1L);
    verify(notificationRepository, times(1)).save(any(Notification.class));
    verify(messagingTemplate, times(1))
        .convertAndSendToUser(eq("1"), eq("/queue/notifications"), any(NotificationMessage.class));
  }

  @Test
  @DisplayName("Should not send notification when blocked by preference")
  void testSendNotificationToUser_BlockedByPreference() {
    testPreference.setServiceUpdates(false);

    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));

    notificationService.sendNotificationToUser(
        1L, "SERVICE_UPDATE", "Title", "Message", new HashMap<>());

    verify(preferenceRepository, times(1)).findByUserId(1L);
    verify(notificationRepository, never()).save(any(Notification.class));
    verify(messagingTemplate, never())
        .convertAndSendToUser(anyString(), anyString(), any(NotificationMessage.class));
  }

  @Test
  @DisplayName("Should create default preference if not exists")
  void testSendNotificationToUser_CreateDefaultPreference() throws Exception {
    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
    when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
    doNothing()
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(NotificationMessage.class));

    notificationService.sendNotificationToUser(
        1L, "SERVICE_UPDATE", "Title", "Message", new HashMap<>());

    verify(preferenceRepository, times(1)).save(any(NotificationPreference.class));
    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  @Test
  @DisplayName("Should notify service progress")
  void testNotifyServiceProgress() throws Exception {
    ServiceProgressUpdate update =
        ServiceProgressUpdate.builder()
            .serviceId(1L)
            .serviceName("Oil Change")
            .customerId(1L)
            .vehicleNumber("ABC-123")
            .status("IN_PROGRESS")
            .progressPercentage(50)
            .currentStage("Draining oil")
            .build();

    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
    doNothing()
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(NotificationMessage.class));
    doNothing().when(messagingTemplate).convertAndSend(anyString(), any(ServiceProgressUpdate.class));

    notificationService.notifyServiceProgress(update);

    verify(notificationRepository, times(1)).save(any(Notification.class));
    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/topic/service-updates"), eq(update));
  }

  @Test
  @DisplayName("Should notify appointment")
  void testNotifyAppointment() throws Exception {
    AppointmentNotification appointmentNotification =
        AppointmentNotification.builder()
            .appointmentId(1L)
            .customerId(1L)
            .appointmentDate("2024-12-25")
            .appointmentTime("10:00")
            .serviceType("Oil Change")
            .vehicleNumber("ABC-123")
            .status("CONFIRMED")
            .message("Your appointment is confirmed")
            .build();

    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
    doNothing()
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(NotificationMessage.class));

    notificationService.notifyAppointment(appointmentNotification);

    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  @Test
  @DisplayName("Should notify project request")
  void testNotifyProjectRequest() throws Exception {
    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
    doNothing()
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(NotificationMessage.class));

    notificationService.notifyProjectRequest(1L, 1L, "Custom Modification", "APPROVED");

    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  @Test
  @DisplayName("Should broadcast announcement")
  void testBroadcastAnnouncement() {
    doNothing().when(messagingTemplate).convertAndSend(eq("/topic/announcements"), any(NotificationMessage.class));

    notificationService.broadcastAnnouncement("Test Announcement", "This is a test");

    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/topic/announcements"), any(NotificationMessage.class));
  }

  @Test
  @DisplayName("Should get user notifications")
  void testGetUserNotifications() {
    Notification notification2 =
        Notification.builder()
            .id(2L)
            .userId(1L)
            .type("APPOINTMENT_UPDATE")
            .title("Appointment Update")
            .message("Your appointment has been confirmed")
            .isRead(true)
            .createdAt(Instant.now())
            .build();

    List<Notification> notifications = Arrays.asList(testNotification, notification2);
    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);

    List<NotificationResponse> result = notificationService.getUserNotifications(1L);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Test Notification", result.get(0).getTitle());
    assertEquals("Appointment Update", result.get(1).getTitle());
    verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
  }

  @Test
  @DisplayName("Should get unread notifications")
  void testGetUnreadNotifications() {
    List<Notification> notifications = Arrays.asList(testNotification);
    when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(1L, false))
        .thenReturn(notifications);

    List<NotificationResponse> result = notificationService.getUnreadNotifications(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertFalse(result.get(0).getIsRead());
    verify(notificationRepository, times(1)).findByUserIdAndIsReadOrderByCreatedAtDesc(1L, false);
  }

  @Test
  @DisplayName("Should get unread count")
  void testGetUnreadCount() {
    when(notificationRepository.countByUserIdAndIsRead(1L, false)).thenReturn(3L);

    Long count = notificationService.getUnreadCount(1L);

    assertEquals(3L, count);
    verify(notificationRepository, times(1)).countByUserIdAndIsRead(1L, false);
  }

  @Test
  @DisplayName("Should mark notification as read")
  void testMarkAsRead() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

    notificationService.markAsRead(1L, 1L);

    assertTrue(testNotification.getIsRead());
    verify(notificationRepository, times(1)).findById(1L);
    verify(notificationRepository, times(1)).save(testNotification);
  }

  @Test
  @DisplayName("Should not mark notification as read if user mismatch")
  void testMarkAsRead_UserMismatch() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

    notificationService.markAsRead(1L, 999L);

    assertFalse(testNotification.getIsRead());
    verify(notificationRepository, times(1)).findById(1L);
    verify(notificationRepository, never()).save(any(Notification.class));
  }

  @Test
  @DisplayName("Should mark all notifications as read")
  void testMarkAllAsRead() {
    Notification notification2 =
        Notification.builder()
            .id(2L)
            .userId(1L)
            .type("APPOINTMENT_UPDATE")
            .title("Appointment")
            .message("Message")
            .isRead(false)
            .build();

    List<Notification> notifications = Arrays.asList(testNotification, notification2);
    when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(1L, false))
        .thenReturn(notifications);
    when(notificationRepository.saveAll(anyList())).thenReturn(notifications);

    notificationService.markAllAsRead(1L);

    assertTrue(testNotification.getIsRead());
    assertTrue(notification2.getIsRead());
    verify(notificationRepository, times(1)).findByUserIdAndIsReadOrderByCreatedAtDesc(1L, false);
    verify(notificationRepository, times(1)).saveAll(notifications);
  }

  @Test
  @DisplayName("Should delete notification")
  void testDeleteNotification() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
    doNothing().when(notificationRepository).delete(testNotification);

    notificationService.deleteNotification(1L, 1L);

    verify(notificationRepository, times(1)).findById(1L);
    verify(notificationRepository, times(1)).delete(testNotification);
  }

  @Test
  @DisplayName("Should not delete notification if user mismatch")
  void testDeleteNotification_UserMismatch() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

    notificationService.deleteNotification(1L, 999L);

    verify(notificationRepository, times(1)).findById(1L);
    verify(notificationRepository, never()).delete(any(Notification.class));
  }

  @Test
  @DisplayName("Should get user preferences")
  void testGetUserPreferences() {
    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));

    NotificationPreferenceResponse result = notificationService.getUserPreferences(1L);

    assertNotNull(result);
    assertEquals(1L, result.getUserId());
    assertTrue(result.getEmailNotifications());
    assertTrue(result.getPushNotifications());
    verify(preferenceRepository, times(1)).findByUserId(1L);
  }

  @Test
  @DisplayName("Should create default preferences if not found")
  void testGetUserPreferences_CreateDefault() {
    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
    when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

    NotificationPreferenceResponse result = notificationService.getUserPreferences(1L);

    assertNotNull(result);
    verify(preferenceRepository, times(1)).findByUserId(1L);
    verify(preferenceRepository, times(1)).save(any(NotificationPreference.class));
  }

  @Test
  @DisplayName("Should update user preferences")
  void testUpdateUserPreferences() {
    NotificationPreferenceRequest request =
        NotificationPreferenceRequest.builder()
            .emailNotifications(false)
            .pushNotifications(true)
            .serviceUpdates(false)
            .appointmentReminders(true)
            .build();

    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
    when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

    NotificationPreferenceResponse result = notificationService.updateUserPreferences(1L, request);

    assertNotNull(result);
    assertFalse(testPreference.getEmailNotifications());
    assertFalse(testPreference.getServiceUpdates());
    verify(preferenceRepository, times(1)).findByUserId(1L);
    verify(preferenceRepository, times(1)).save(testPreference);
  }

  @Test
  @DisplayName("Should update only non-null preference fields")
  void testUpdateUserPreferences_PartialUpdate() {
    NotificationPreferenceRequest request =
        NotificationPreferenceRequest.builder().emailNotifications(false).build();

    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
    when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreference);

    notificationService.updateUserPreferences(1L, request);

    assertFalse(testPreference.getEmailNotifications());
    assertTrue(testPreference.getPushNotifications()); // Should remain unchanged
    verify(preferenceRepository, times(1)).save(testPreference);
  }

  @Test
  @DisplayName("Should not send notification when push notifications disabled")
  void testSendNotificationToUser_PushDisabled() {
    testPreference.setPushNotifications(false);

    when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));

    notificationService.sendNotificationToUser(
        1L, "SERVICE_UPDATE", "Title", "Message", new HashMap<>());

    verify(notificationRepository, never()).save(any(Notification.class));
  }
}
