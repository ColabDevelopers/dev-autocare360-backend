package com.autocare360.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDto {
    /*private Long taskId;
    private String taskType;
    private String taskName;
    private LocalDateTime scheduledDate;
    private LocalDateTime dueDate;
    private String make;
    private String model;
    private String registrationNumber;
    private String priority;
    private String status;
    private Integer progress;
    private String vehicleInfo;*/

    private Long taskId;
    private String taskType;
    private String taskName;
    private String status;
    private LocalDateTime scheduledDate;
    private LocalDateTime dueDate;
    private String priority;
    private Integer progress;
    private String vehicleInfo;
}
