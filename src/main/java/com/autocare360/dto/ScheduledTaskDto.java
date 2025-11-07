package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskDto {
    private Long taskId;
    private String taskName;
    private String startTime;
    private String endTime;
    private String duration;
    private String status;
    private String priority;
    private String customerName;
    private String vehicleInfo;
}
