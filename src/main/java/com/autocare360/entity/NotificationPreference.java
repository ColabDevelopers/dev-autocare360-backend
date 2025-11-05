package com.autocare360.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "email_notifications", nullable = false)
  @Builder.Default
  private Boolean emailNotifications = true;

  @Column(name = "push_notifications", nullable = false)
  @Builder.Default
  private Boolean pushNotifications = true;

  @Column(name = "service_updates", nullable = false)
  @Builder.Default
  private Boolean serviceUpdates = true;

  @Column(name = "appointment_reminders", nullable = false)
  @Builder.Default
  private Boolean appointmentReminders = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
