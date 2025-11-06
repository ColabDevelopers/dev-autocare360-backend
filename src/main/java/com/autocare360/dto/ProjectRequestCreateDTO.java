package com.autocare360.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestCreateDTO {
    
    @NotBlank(message = "Project name is required")
    private String projectName;
    
    @NotBlank(message = "Project type is required")
    private String projectType; // "MODIFICATION", "CUSTOM_WORK", "UPGRADE", "REPAIR"
    
    private String vehicleDetails;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String priority = "MEDIUM"; // "LOW", "MEDIUM", "HIGH", "URGENT"
    
    private BigDecimal estimatedCost;
    
    private Integer estimatedDurationDays;
    
    private String attachments; // JSON string containing file URLs
    
    // Optional field to specify request date (defaults to current date if not provided)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestedAt;
    
    // This will be set from JWT token in controller
    private Long customerId;
}