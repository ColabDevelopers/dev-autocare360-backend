package com.autocare360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateTimeLogRequestDTO {

  private Long appointmentId;
  private BigDecimal hours;
  private String description;
  private LocalDate date;
  private String status;

  // Constructors
  public UpdateTimeLogRequestDTO() {}

  // Getters and Setters
  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  public BigDecimal getHours() {
    return hours;
  }

  public void setHours(BigDecimal hours) {
    this.hours = hours;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
