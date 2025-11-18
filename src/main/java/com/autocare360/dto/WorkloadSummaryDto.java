package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadSummaryDto {
    private int totalEmployees;
    private double averageCapacity;
    private int activeItems;     // <-- must match builder usage
    private double utilizationRate;
}
