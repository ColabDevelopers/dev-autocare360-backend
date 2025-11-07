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
public class EmployeeScheduleDto {
    private Long employeeId;
    private String name;
    private String email;
    private String department;
    private String status;
    private List<DailyScheduleDto> upcomingSchedule;
}

/*@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DailyScheduleDto {
    private String date; // "2025-11-07"
    private String dayOfWeek; // "Thursday"
    private List<ScheduledTaskDto> tasks;
    private Double totalHours;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ScheduledTaskDto {
    private Long taskId;
    private String taskName;
    private String startTime; // "09:00 AM"
    private String endTime; // "11:00 AM"
    private String duration; // "2h"
    private String status;
    private String priority;
    private String customerName;
    private String vehicleInfo;
}*/