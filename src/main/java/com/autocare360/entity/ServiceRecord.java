package com.autocare360.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "vehicle_id")
  private Long vehicleId;

  @Column(name = "type")
  private String type; // Category: "maintenance", "safety", "diagnostics"

  @Column(name = "name")
  private String name; // Service name like "Oil Change", "Brake Inspection"

  @Column(name = "status")
  private String status; // "active", "inactive", "requested", "scheduled", "completed"

  @Column(name = "requested_at")
  private LocalDateTime requestedAt;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes; // Description

  @Column(name = "attachments", columnDefinition = "JSON")
  private String attachments; // Stored as JSON string

  @Column(name = "price")
  private Double price;

  @Column(name = "duration")
  private Double duration; // Duration in hours

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (requestedAt == null) {
      requestedAt = LocalDateTime.now();
    }
    if (status == null) {
      status = "requested";
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
