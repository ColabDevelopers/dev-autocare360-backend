package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentRequest {
    private Long taskId;
    private Long employeeId;
    private String notes;
}