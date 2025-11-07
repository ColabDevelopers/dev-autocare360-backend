package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnassignedTaskDto {
    private Long id;
    private String title;
    private String customerName;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String type; // appointment, project
    private String dueDate;
    private Integer estimatedHours;
    private String vehicleInfo;
    private String description;
}