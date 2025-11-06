package com.autocare360.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TimerResponseDTO {

  private Long timerId;
  private Long appointmentId;
  private String projectName;
  private LocalDateTime startTime;
  private Integer elapsedSeconds;
  private BigDecimal elapsedHours;
  private Boolean isActive;

  // Constructors
  public TimerResponseDTO() {}

  // Getters and Setters
  public Long getTimerId() {
    return timerId;
  }

  public void setTimerId(Long timerId) {
    this.timerId = timerId;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public Integer getElapsedSeconds() {
    return elapsedSeconds;
  }

  public void setElapsedSeconds(Integer elapsedSeconds) {
    this.elapsedSeconds = elapsedSeconds;
  }

  public BigDecimal getElapsedHours() {
    return elapsedHours;
  }

  public void setElapsedHours(BigDecimal elapsedHours) {
    this.elapsedHours = elapsedHours;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }
}
