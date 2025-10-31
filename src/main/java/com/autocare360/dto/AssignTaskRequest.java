package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTaskRequest {
    @NotNull(message = "Work item ID is required")
    private Long workItemId;
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    private String roleOnJob; // Optional, defaults to "Technician"
}