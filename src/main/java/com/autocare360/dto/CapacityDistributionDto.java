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
    /** Capacity utilisation for this employee, as a percentage 0–100 */
    private Double capacityPercentage;
    /** Hex color used by the chart (frontend can override if needed) */
    private String color;
}
