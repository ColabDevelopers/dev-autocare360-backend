package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private Integer totalEmployees;
    private Integer activeEmployees;
    private Integer totalAppointments;
    private Integer pendingAppointments;
    private Integer inProgressAppointments;
    private Integer completedAppointments;
    private BigDecimal averageCompletionTime;
    private Integer totalCustomers;
    private Double employeeUtilizationRate;
    private Integer activeProjects;
}