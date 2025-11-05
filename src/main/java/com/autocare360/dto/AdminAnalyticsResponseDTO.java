package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsResponseDTO {
    private LocalDate reportDate;
    private String reportType;
    private AnalyticsStats currentStats;
    private List<TrendPoint> trends;
    private ComparisonStats comparison;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsStats {
        private Integer totalEmployees;
        private Integer activeEmployees;
        private Integer totalAppointments;
        private Integer completedAppointments;
        private Integer pendingAppointments;
        private Integer inProgressAppointments;
        private BigDecimal averageCompletionTime;
        private BigDecimal employeeUtilizationRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private LocalDate date;
        private BigDecimal value;
        private String metric;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonStats {
        private BigDecimal employeeUtilizationChange;
        private BigDecimal completionRateChange;
        private Integer newAppointmentsChange;
        private String period; // "vs last week" or "vs last month"
    }
}