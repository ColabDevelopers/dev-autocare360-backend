package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityMetrics {
    private Integer totalEmployees;
    private Integer availableEmployees;
    private Integer busyEmployees;
    private Integer overloadedEmployees;
    private Double averageCapacity;
    private Integer totalActiveWorkItems;
}