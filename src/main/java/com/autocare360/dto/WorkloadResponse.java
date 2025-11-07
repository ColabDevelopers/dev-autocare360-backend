package com.autocare360.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkloadResponse {
    private Long employeeId;
    private String name;
    private String email;
    private String department;
    private String workloadStatus;
    private Double capacityUtilization;
    private Integer activeAppointments;
    private Integer activeProjects;
    private Double hoursLoggedThisWeek;
    private Double hoursLoggedThisMonth;
    private List<TaskSummaryDto> upcomingTasks;
    private List<TaskSummaryDto> activeTasks;

    /*private Long employeeId;
    private String name;
    private String email;
    private String department;
    private String workloadStatus;
    private Double capacityUtilization;
    private Integer activeAppointments;
    private Integer activeProjects;
    private Double hoursLoggedThisWeek;
    private Double hoursLoggedThisMonth;
    private List<TaskSummaryDto> upcomingTasks;
    private List<TaskSummaryDto> activeTasks;*/
}
