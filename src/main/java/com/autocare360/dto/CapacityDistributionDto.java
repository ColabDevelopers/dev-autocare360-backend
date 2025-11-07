package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityDistributionDto {
    private Long employeeId;
    private String name;
    private String department;
    private Double capacityPercentage;
    private String status; // AVAILABLE, BUSY, OVERLOADED
    private Integer activeTaskCount;
}