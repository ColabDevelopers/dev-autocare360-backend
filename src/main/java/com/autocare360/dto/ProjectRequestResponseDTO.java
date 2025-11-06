package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestResponseDTO {
    
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
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
    private String assignedEmployeeName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}