package com.autocare360.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "project_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private User customer;
    
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;
    
    @Column(name = "project_type", nullable = false, length = 100)
    private String projectType; // "MODIFICATION", "CUSTOM_WORK", "UPGRADE", "REPAIR"
    
    @Column(name = "vehicle_details", length = 500)
    private String vehicleDetails; // Make, model, year, etc.
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "priority", length = 20)
    private String priority = "MEDIUM"; // "LOW", "MEDIUM", "HIGH", "URGENT"
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;
    
    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;
    
    @Column(name = "approved_cost", precision = 10, scale = 2)
    private BigDecimal approvedCost;
    
    @Column(name = "actual_cost", precision = 10, scale = 2)
    private BigDecimal actualCost;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "completion_date")
    private LocalDateTime completionDate;
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments; // Store as JSON string containing file URLs
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    @JsonIgnore
    private Employee assignedEmployee;
    
    @Column(name = "requested_at")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestedAt;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }
    
    public void setCustomerId(Long customerId) {
        if (this.customer == null) {
            this.customer = new User();
        }
        this.customer.setId(customerId);
    }
    
    public String getCustomerName() {
        return customer != null ? customer.getName() : null;
    }
    
    public String getCustomerEmail() {
        return customer != null ? customer.getEmail() : null;
    }
    
    public Long getAssignedEmployeeId() {
        return assignedEmployee != null ? assignedEmployee.getId() : null;
    }
    
    public void setAssignedEmployeeId(Long employeeId) {
        if (employeeId != null) {
            if (this.assignedEmployee == null) {
                this.assignedEmployee = new Employee();
            }
            this.assignedEmployee.setId(employeeId);
        } else {
            this.assignedEmployee = null;
        }
    }
    
    public String getAssignedEmployeeName() {
        return assignedEmployee != null ? assignedEmployee.getName() : null;
    }
    
    // Explicit getter/setter for requestedAt to ensure it works properly
    public LocalDate getRequestedAt() {
        return requestedAt;
    }
    
    public void setRequestedAt(LocalDate requestedAt) {
        this.requestedAt = requestedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestedAt == null) {
            requestedAt = LocalDate.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (priority == null) {
            priority = "MEDIUM";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}