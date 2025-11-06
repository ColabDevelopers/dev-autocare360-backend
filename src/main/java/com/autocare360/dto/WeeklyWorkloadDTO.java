package com.autocare360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WeeklyWorkloadDTO {
  private String dayName;
  private LocalDate date;
  private BigDecimal hours;
  private Integer dayOfWeek;

  public WeeklyWorkloadDTO() {}

  public WeeklyWorkloadDTO(String dayName, LocalDate date, BigDecimal hours, Integer dayOfWeek) {
    this.dayName = dayName;
    this.date = date;
    this.hours = hours;
    this.dayOfWeek = dayOfWeek;
  }

  // Getters and Setters
  public String getDayName() {
    return dayName;
  }

  public void setDayName(String dayName) {
    this.dayName = dayName;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public BigDecimal getHours() {
    return hours;
  }

  public void setHours(BigDecimal hours) {
    this.hours = hours;
  }

  public Integer getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(Integer dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }
}
