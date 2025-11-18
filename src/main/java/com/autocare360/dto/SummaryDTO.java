package com.autocare360.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDTO {
    private double totalRevenue;
    private int servicesCompleted;
    private double customerSatisfaction;
    private double avgServiceTime;
}
