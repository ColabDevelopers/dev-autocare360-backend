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
public class TaskDto {
    private Long id;
    private Long workItemId;
    private String title;
    private String type; // "appointment" or "project"
    private String scheduledDate;
    private Double estimatedHours;
    private String status;
    private String customerName;
}