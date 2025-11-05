package com.autocare360.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "report_type", nullable = false)
    private String reportType;  // DAILY, WEEKLY, MONTHLY

    @Column(name = "total_employees", nullable = false)
    private Integer totalEmployees;

    @Column(name = "active_employees", nullable = false)
    private Integer activeEmployees;

    @Column(name = "total_appointments", nullable = false)
    private Integer totalAppointments;

    @Column(name = "completed_appointments", nullable = false)
    private Integer completedAppointments;

    @Column(name = "pending_appointments", nullable = false)
    private Integer pendingAppointments;

    @Column(name = "in_progress_appointments", nullable = false)
    private Integer inProgressAppointments;

    @Column(name = "average_completion_time")
    private BigDecimal averageCompletionTime;

    @Column(name = "employee_utilization_rate")
    private BigDecimal employeeUtilizationRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}