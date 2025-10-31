package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadResponse {
    private Long employeeId;
    private String name;
    private String email;
    private String department;
    private Integer activeAppointments;
    private Integer activeProjects;
    private Double hoursLoggedThisWeek;
    private Double hoursLoggedThisMonth;
    private Double capacityUtilization;
    private String status; // "available", "busy", "overloaded"
    private List<TaskDto> upcomingTasks;
}