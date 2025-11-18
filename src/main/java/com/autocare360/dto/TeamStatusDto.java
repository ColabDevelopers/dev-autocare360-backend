package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatusDto {
    /** Total employees considered in workload monitoring */
    private int total;
    private int availableCount;
    private int busyCount;
    private int overloadedCount;
}
