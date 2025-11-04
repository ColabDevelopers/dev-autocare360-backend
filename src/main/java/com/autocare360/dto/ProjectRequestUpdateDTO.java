package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestUpdateDTO {
    
    private String projectName;
    private String projectType;
    private String vehicleDetails;
    private String description;
    private String priority;
    private String status;
    private BigDecimal estimatedCost;
    private Integer estimatedDurationDays;
    private BigDecimal approvedCost;
    private BigDecimal actualCost;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private String adminNotes;
    private String rejectionReason;
    private String attachments;
    private Long assignedEmployeeId;
}