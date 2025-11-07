package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkloadSummaryDto {
    private int totalEmployees;
    private int availableCount;
    private int busyCount;
    private int overloadedCount;
    private double averageUtilization;
    private int totalActiveTasks;
}
