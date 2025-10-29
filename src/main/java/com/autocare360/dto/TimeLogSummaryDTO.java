package com.autocare360.dto;

import java.math.BigDecimal;

public class TimeLogSummaryDTO {
    
    private BigDecimal totalHoursToday;
    private BigDecimal totalHoursWeek;
    private Integer totalEntries;
    private BigDecimal efficiencyRate; // Percentage
    
    // Constructors
    public TimeLogSummaryDTO() {
    }
    
    public TimeLogSummaryDTO(BigDecimal totalHoursToday, BigDecimal totalHoursWeek, 
                             Integer totalEntries, BigDecimal efficiencyRate) {
        this.totalHoursToday = totalHoursToday;
        this.totalHoursWeek = totalHoursWeek;
        this.totalEntries = totalEntries;
        this.efficiencyRate = efficiencyRate;
    }
    
    // Getters and Setters
    public BigDecimal getTotalHoursToday() {
        return totalHoursToday;
    }
    
    public void setTotalHoursToday(BigDecimal totalHoursToday) {
        this.totalHoursToday = totalHoursToday;
    }
    
    public BigDecimal getTotalHoursWeek() {
        return totalHoursWeek;
    }
    
    public void setTotalHoursWeek(BigDecimal totalHoursWeek) {
        this.totalHoursWeek = totalHoursWeek;
    }
    
    public Integer getTotalEntries() {
        return totalEntries;
    }
    
    public void setTotalEntries(Integer totalEntries) {
        this.totalEntries = totalEntries;
    }
    
    public BigDecimal getEfficiencyRate() {
        return efficiencyRate;
    }
    
    public void setEfficiencyRate(BigDecimal efficiencyRate) {
        this.efficiencyRate = efficiencyRate;
    }
}
