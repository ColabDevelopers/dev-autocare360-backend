package com.autocare360.dto;

import java.math.BigDecimal;

public class EmployeeDashboardSummaryDTO {
  private Integer activeJobs;
  private Integer jobsInProgress;
  private BigDecimal todayHours;
  private BigDecimal targetHours;
  private Integer completedThisMonth;
  private BigDecimal efficiencyRate;
  private String employeeName;
  private String greeting;

  public EmployeeDashboardSummaryDTO() {}

  public EmployeeDashboardSummaryDTO(
      Integer activeJobs,
      Integer jobsInProgress,
      BigDecimal todayHours,
      BigDecimal targetHours,
      Integer completedThisMonth,
      BigDecimal efficiencyRate,
      String employeeName,
      String greeting) {
    this.activeJobs = activeJobs;
    this.jobsInProgress = jobsInProgress;
    this.todayHours = todayHours;
    this.targetHours = targetHours;
    this.completedThisMonth = completedThisMonth;
    this.efficiencyRate = efficiencyRate;
    this.employeeName = employeeName;
    this.greeting = greeting;
  }

  // Getters and Setters
  public Integer getActiveJobs() {
    return activeJobs;
  }

  public void setActiveJobs(Integer activeJobs) {
    this.activeJobs = activeJobs;
  }

  public Integer getJobsInProgress() {
    return jobsInProgress;
  }

  public void setJobsInProgress(Integer jobsInProgress) {
    this.jobsInProgress = jobsInProgress;
  }

  public BigDecimal getTodayHours() {
    return todayHours;
  }

  public void setTodayHours(BigDecimal todayHours) {
    this.todayHours = todayHours;
  }

  public BigDecimal getTargetHours() {
    return targetHours;
  }

  public void setTargetHours(BigDecimal targetHours) {
    this.targetHours = targetHours;
  }

  public Integer getCompletedThisMonth() {
    return completedThisMonth;
  }

  public void setCompletedThisMonth(Integer completedThisMonth) {
    this.completedThisMonth = completedThisMonth;
  }

  public BigDecimal getEfficiencyRate() {
    return efficiencyRate;
  }

  public void setEfficiencyRate(BigDecimal efficiencyRate) {
    this.efficiencyRate = efficiencyRate;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setEmployeeName(String employeeName) {
    this.employeeName = employeeName;
  }

  public String getGreeting() {
    return greeting;
  }

  public void setGreeting(String greeting) {
    this.greeting = greeting;
  }
}
