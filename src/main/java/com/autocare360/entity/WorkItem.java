package com.autocare360.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "work_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(nullable = false, length = 50)
    private String type; // appointment, project
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "pending"; // pending, in_progress, completed, cancelled
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "estimated_hours")
    private Double estimatedHours;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "scheduled_at")
    private Instant scheduledAt;
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = "pending";
        }
    }
    
    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
